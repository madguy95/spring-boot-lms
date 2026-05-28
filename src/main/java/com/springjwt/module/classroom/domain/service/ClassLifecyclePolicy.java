package com.springjwt.module.classroom.domain.service;

import com.springjwt.common.exception.AppException;
import com.springjwt.module.classroom.domain.entity.ClassEntity;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Set;

/**
 * Class lifecycle + editability rules. Stored status drives transitions; display
 * status (derived from lifecycle + dates + capacity) drives the editing matrix.
 *
 * Stored:  draft, published, unpublished, cancelled
 * Derived: open, full, ongoing, completed (only when lifecycle = published)
 *
 * Keep these rules in ONE place — the service layer calls these helpers rather
 * than re-encoding the matrix in each endpoint.
 */
@Component
public class ClassLifecyclePolicy {

    public static final String LIFECYCLE_DRAFT = "draft";
    public static final String LIFECYCLE_PUBLISHED = "published";
    public static final String LIFECYCLE_UNPUBLISHED = "unpublished";
    public static final String LIFECYCLE_CANCELLED = "cancelled";

    public static final String DISPLAY_DRAFT = "draft";
    public static final String DISPLAY_OPEN = "open";
    public static final String DISPLAY_FULL = "full";
    public static final String DISPLAY_ONGOING = "ongoing";
    public static final String DISPLAY_COMPLETED = "completed";
    public static final String DISPLAY_UNPUBLISHED = "unpublished";
    public static final String DISPLAY_CANCELLED = "cancelled";

    // Editable field identifiers — matched against UpdateClassRequest setters
    // in ClassServiceImpl. Keep this set in sync with the matrix below.
    public static final String F_LABEL = "label";
    public static final String F_DESCRIPTION = "description";
    public static final String F_COVER = "cover";
    public static final String F_COURSE = "courseId";
    public static final String F_TEACHER = "teacherId";
    public static final String F_LOCATION = "location";
    public static final String F_CAPACITY = "capacity";
    public static final String F_DAY_SCHEDULES = "daySchedules";
    public static final String F_START_DATE = "startDate";
    public static final String F_END_DATE = "endDate";
    public static final String F_VISIBILITY = "visibility";

    // --- derived display status -----------------------------------------

    public String deriveDisplayStatus(ClassEntity cls, LocalDate today) {
        String lifecycle = cls.getLifecycleStatus();
        if (LIFECYCLE_CANCELLED.equals(lifecycle)) return DISPLAY_CANCELLED;
        if (LIFECYCLE_DRAFT.equals(lifecycle))     return DISPLAY_DRAFT;
        if (LIFECYCLE_UNPUBLISHED.equals(lifecycle)) return DISPLAY_UNPUBLISHED;
        // published branch
        if (today.isAfter(cls.getEndDate()))   return DISPLAY_COMPLETED;
        if (!today.isBefore(cls.getStartDate())) return DISPLAY_ONGOING; // today >= startDate && today <= endDate
        if (cls.getEnrolled() != null && cls.getCapacity() != null
                && cls.getEnrolled() >= cls.getCapacity()) return DISPLAY_FULL;
        return DISPLAY_OPEN;
    }

    // --- transitions ----------------------------------------------------

    /**
     * Validate a lifecycle action and return the resulting stored lifecycle.
     * Throws if the action is illegal from the current state.
     */
    public String resolveTransition(ClassEntity cls, String action, LocalDate today) {
        String life = cls.getLifecycleStatus();
        String display = deriveDisplayStatus(cls, today);

        return switch (action) {
            case "publish" -> {
                if (!LIFECYCLE_DRAFT.equals(life) && !LIFECYCLE_UNPUBLISHED.equals(life)) {
                    throw transitionError(life, action);
                }
                // Can re-publish from UNPUBLISHED only if not already past end_date.
                if (LIFECYCLE_UNPUBLISHED.equals(life) && today.isAfter(cls.getEndDate())) {
                    throw new AppException("class.lifecycle.cannot.republish.completed", HttpStatus.CONFLICT);
                }
                yield LIFECYCLE_PUBLISHED;
            }
            case "unpublish" -> {
                if (!LIFECYCLE_PUBLISHED.equals(life)) throw transitionError(life, action);
                // Blocked once the class is in session or finished — derived states
                // ONGOING / COMPLETED both mean students are already in the schedule.
                if (DISPLAY_ONGOING.equals(display) || DISPLAY_COMPLETED.equals(display)) {
                    throw new AppException("class.lifecycle.cannot.unpublish.ongoing", HttpStatus.CONFLICT);
                }
                yield LIFECYCLE_UNPUBLISHED;
            }
            case "cancel" -> {
                if (LIFECYCLE_CANCELLED.equals(life)) throw transitionError(life, action);
                if (DISPLAY_COMPLETED.equals(display)) {
                    throw new AppException("class.lifecycle.cannot.cancel.completed", HttpStatus.CONFLICT);
                }
                yield LIFECYCLE_CANCELLED;
            }
            default -> throw new AppException("class.lifecycle.action.unknown", HttpStatus.BAD_REQUEST);
        };
    }

    // --- editability matrix --------------------------------------------

    /**
     * `true` if `field` can be modified given the class's current state.
     * Note: this does NOT validate the new value (e.g. capacity >= enrolled is
     * checked by the caller). It only answers "is this field touchable at all?".
     */
    public boolean canEdit(String field, ClassEntity cls, LocalDate today) {
        String life = cls.getLifecycleStatus();

        if (LIFECYCLE_CANCELLED.equals(life)) {
            return false; // only cancellation_reason is editable, handled by a dedicated endpoint
        }
        if (LIFECYCLE_DRAFT.equals(life)) {
            return true; // full edit
        }

        String display = deriveDisplayStatus(cls, today);

        if (DISPLAY_COMPLETED.equals(display)) {
            // read-only except admin meta (visibility toggle)
            return F_VISIBILITY.equals(field);
        }
        if (DISPLAY_ONGOING.equals(display)) {
            return ONGOING_EDITABLE.contains(field);
        }

        boolean hasEnroll = cls.getEnrolled() != null && cls.getEnrolled() > 0;
        // PUBLISHED or UNPUBLISHED, before start_date.
        // UNPUBLISHED behaves like DRAFT for editability purposes (admin can fix
        // mistakes before re-publishing) UNLESS students were already enrolled
        // before unpublishing — in which case we treat it like PUBLISHED+hasEnroll.
        if (LIFECYCLE_UNPUBLISHED.equals(life) && !hasEnroll) {
            return true;
        }

        if (hasEnroll) {
            return PUBLISHED_HAS_ENROLL_EDITABLE.contains(field);
        }
        return true; // PUBLISHED, before start, no enrollments → full edit
    }

    public void requireEditable(String field, ClassEntity cls, LocalDate today) {
        if (!canEdit(field, cls, today)) {
            throw new AppException("class.edit.field.locked", HttpStatus.CONFLICT);
        }
    }

    /** Validate new capacity respects current enrolment. */
    public void validateCapacityChange(int newCapacity, int currentEnrolled) {
        if (newCapacity < currentEnrolled) {
            throw new AppException("class.edit.capacity.below.enrolled", HttpStatus.CONFLICT);
        }
    }

    /** When ONGOING, end_date can only be extended, never shortened. */
    public void validateEndDateChange(LocalDate newEnd, LocalDate currentEnd,
                                      ClassEntity cls, LocalDate today) {
        String display = deriveDisplayStatus(cls, today);
        if (DISPLAY_ONGOING.equals(display) && newEnd.isBefore(currentEnd)) {
            throw new AppException("class.edit.endDate.cannot.shorten.ongoing", HttpStatus.CONFLICT);
        }
    }

    /** When editing start_date while published+hasEnroll, the new date must be future. */
    public void validateStartDateChange(LocalDate newStart, LocalDate today,
                                        ClassEntity cls) {
        boolean hasEnroll = cls.getEnrolled() != null && cls.getEnrolled() > 0;
        if (hasEnroll && !newStart.isAfter(today)) {
            throw new AppException("class.edit.startDate.must.be.future", HttpStatus.CONFLICT);
        }
    }

    // Fields editable while ONGOING (matrix row 4).
    private static final Set<String> ONGOING_EDITABLE = Set.of(
            F_DESCRIPTION, F_COVER, F_TEACHER, F_LOCATION, F_VISIBILITY, F_END_DATE
    );

    // Fields editable while PUBLISHED + hasEnrollments + beforeStart (matrix row 3).
    // `label` and `courseId` are locked because changing them after students see
    // the class would surface a different product than what they enrolled in.
    private static final Set<String> PUBLISHED_HAS_ENROLL_EDITABLE = Set.of(
            F_DESCRIPTION, F_COVER, F_TEACHER, F_LOCATION,
            F_CAPACITY, F_DAY_SCHEDULES, F_START_DATE, F_END_DATE, F_VISIBILITY
    );

    private AppException transitionError(String from, String action) {
        return new AppException("class.lifecycle.transition.invalid", HttpStatus.CONFLICT);
    }
}
