package com.springjwt.module.course.business.impl;

import com.springjwt.common.exception.AppException;
import com.springjwt.module.course.business.CourseService;
import com.springjwt.module.course.domain.entity.Course;
import com.springjwt.module.course.domain.entity.CourseDiscount;
import com.springjwt.module.course.domain.entity.CourseSession;
import com.springjwt.module.course.domain.repository.CourseRepository;
import com.springjwt.module.course.domain.service.CourseDomainService;
import com.springjwt.module.course.model.dto.*;
import com.springjwt.module.course.model.request.*;
import com.springjwt.module.masterdata.business.MasterDataService;
import com.springjwt.module.masterdata.domain.entity.MasterData;
import com.springjwt.module.masterdata.domain.repository.MasterDataRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

@Service
@RequiredArgsConstructor
public class CourseServiceImpl implements CourseService {

    private static final String STATUS_PUBLISHED = "published";
    private static final String STATUS_DRAFT = "draft";
    private static final String STATUS_UNPUBLISHED = "unpublished";
    private static final String MASTER_TYPE_TOOL = "tool";

    private final CourseRepository courseRepository;
    private final CourseDomainService courseDomainService;
    private final MasterDataService masterDataService;
    private final MasterDataRepository masterDataRepository;

    @Override
    public Page<CourseDto> listCourses(CourseListRequest request) {
        Sort sort = buildSort(request.getSortBy(), request.getSortDirection());
        PageRequest pageable = PageRequest.of(Math.max(request.getPage() - 1, 0), request.getSize(), sort);
        String search = normalize(request.getSearch());

        Page<Course> courses = courseRepository.search(
                request.getTool(), request.getStatus(), search, pageable);
        List<CourseDto> data = courses.stream().map(this::toDto).toList();
        return new PageImpl<>(data, pageable, courses.getTotalElements());
    }

    @Override
    public CourseDto getCourseById(Long id) {
        return toDto(courseDomainService.getCourseById(id));
    }

    @Override
    @Transactional
    public CourseDto createCourse(CreateCourseRequest request) {
        courseDomainService.validateUniqueCode(request.getCode().trim(), null);
        courseDomainService.validateAgeRange(request.getMinAge(), request.getMaxAge());
        masterDataService.validateCodeExists(MASTER_TYPE_TOOL, request.getTool());

        boolean hasNumericDiscount = hasNumericDiscount(request.getDiscounts());
        BigDecimal finalTuition = applyDiscounts(request.getTuitionAmount(), request.getDiscounts());

        Course course = Course.builder()
                .code(request.getCode().trim())
                .title(request.getTitle().trim())
                .tagline(buildTagline(request.getTags(), null))
                .description(trimToNull(request.getDescription()))
                .tool(request.getTool())
                .status(STATUS_DRAFT)
                .minAge(request.getMinAge())
                .maxAge(request.getMaxAge())
                .totalSessions(request.getTotalSessions())
                .sessionDurationMinutes(request.getSessionDurationMinutes())
                .perClassCapacity(request.getPerClassCapacity())
                .tuitionAmount(finalTuition)
                .originalTuitionAmount(hasNumericDiscount ? request.getTuitionAmount() : null)
                .pricingNotes(trimToNull(request.getPricingNotes()))
                .coverUrl(trimToNull(request.getCoverUrl()))
                .introVideoUrl(trimToNull(request.getIntroVideoUrl()))
                .versionLabel("v0.1")
                .build();

        replaceSessions(course, request.getSessions());
        replaceDiscounts(course, request.getDiscounts());

        return toDto(courseRepository.save(course));
    }

    @Override
    @Transactional
    public CourseDto updateCourse(Long id, UpdateCourseRequest request) {
        Course course = courseDomainService.getCourseById(id);

        if (request.getCode() != null) {
            String code = request.getCode().trim();
            courseDomainService.validateUniqueCode(code, course.getId());
            course.setCode(code);
        }
        if (request.getTitle() != null)        course.setTitle(request.getTitle().trim());
        if (request.getDescription() != null)  course.setDescription(trimToNull(request.getDescription()));
        if (request.getTool() != null) {
            masterDataService.validateCodeExists(MASTER_TYPE_TOOL, request.getTool());
            course.setTool(request.getTool());
        }
        if (request.getStatus() != null)       course.setStatus(request.getStatus());
        if (request.getMinAge() != null)       course.setMinAge(request.getMinAge());
        if (request.getMaxAge() != null)       course.setMaxAge(request.getMaxAge());
        courseDomainService.validateAgeRange(course.getMinAge(), course.getMaxAge());

        if (request.getTotalSessions() != null)         course.setTotalSessions(request.getTotalSessions());
        if (request.getSessionDurationMinutes() != null) course.setSessionDurationMinutes(request.getSessionDurationMinutes());
        if (request.getPerClassCapacity() != null)      course.setPerClassCapacity(request.getPerClassCapacity());
        if (request.getPricingNotes() != null)          course.setPricingNotes(trimToNull(request.getPricingNotes()));

        if (request.getTags() != null) {
            course.setTagline(buildTagline(request.getTags(), course.getTagline()));
        }

        // Recompute tuition only when pricing inputs touched, mirroring FE behaviour.
        boolean pricingTouched = request.getTuitionAmount() != null || request.getDiscounts() != null;
        if (pricingTouched) {
            List<DiscountRuleInput> nextDiscounts = request.getDiscounts() != null
                    ? request.getDiscounts()
                    : toDiscountInputs(course.getDiscounts());
            if (request.getTuitionAmount() != null) {
                boolean hasNumeric = hasNumericDiscount(nextDiscounts);
                BigDecimal recomputed = applyDiscounts(request.getTuitionAmount(), nextDiscounts);
                course.setTuitionAmount(recomputed);
                course.setOriginalTuitionAmount(hasNumeric ? request.getTuitionAmount() : null);
            }
            if (request.getDiscounts() != null) {
                replaceDiscounts(course, request.getDiscounts());
            }
        }

        if (request.getSessions() != null) {
            replaceSessions(course, request.getSessions());
        }

        if (request.getCoverUrl() != null) {
            course.setCoverUrl(trimToNull(request.getCoverUrl()));
        }
        if (request.getIntroVideoUrl() != null) {
            course.setIntroVideoUrl(trimToNull(request.getIntroVideoUrl()));
        }

        course.setVersionLabel(bumpVersion(course.getVersionLabel()));
        return toDto(courseRepository.save(course));
    }

    @Override
    @Transactional
    public CourseDto updateCourseStatus(Long id, UpdateCourseStatusRequest request) {
        Course course = courseDomainService.getCourseById(id);
        course.setStatus(request.getStatus());
        return toDto(courseRepository.save(course));
    }

    @Override
    @Transactional
    public CourseDto duplicateCourse(Long id, DuplicateCourseRequest request) {
        Course source = courseDomainService.getCourseById(id);

        String newCode = request.getCode() != null && !request.getCode().isBlank()
                ? request.getCode().trim()
                : source.getCode() + "-COPY";
        courseDomainService.validateUniqueCode(newCode, null);

        String newTitle = request.getTitle() != null && !request.getTitle().isBlank()
                ? request.getTitle().trim()
                : source.getTitle() + " (copy)";

        Course copy = Course.builder()
                .code(newCode)
                .title(newTitle)
                .tagline(source.getTagline())
                .description(source.getDescription())
                .tool(source.getTool())
                .status(STATUS_DRAFT)
                .minAge(source.getMinAge())
                .maxAge(source.getMaxAge())
                .totalSessions(source.getTotalSessions())
                .sessionDurationMinutes(source.getSessionDurationMinutes())
                .perClassCapacity(source.getPerClassCapacity())
                .tuitionAmount(source.getTuitionAmount())
                .originalTuitionAmount(source.getOriginalTuitionAmount())
                .pricingNotes(source.getPricingNotes())
                .versionLabel("v0.1")
                .build();

        for (CourseSession s : source.getSessions()) {
            copy.getSessions().add(CourseSession.builder()
                    .course(copy)
                    .position(s.getPosition())
                    .title(s.getTitle())
                    .description(s.getDescription())
                    .build());
        }
        for (CourseDiscount d : source.getDiscounts()) {
            copy.getDiscounts().add(CourseDiscount.builder()
                    .course(copy)
                    .position(d.getPosition())
                    .name(d.getName())
                    .type(d.getType())
                    .valueNumeric(d.getValueNumeric())
                    .valueText(d.getValueText())
                    .condition(d.getCondition())
                    .conditionDate(d.getConditionDate())
                    .build());
        }

        return toDto(courseRepository.save(copy));
    }

    @Override
    @Transactional
    public void deleteCourse(Long id) {
        Course course = courseDomainService.getCourseById(id);
        // Only drafts can be removed. Published/unpublished courses must be
        // archived; allowing deletion would lose enrolment history once that
        // module exists.
        if (!STATUS_DRAFT.equals(course.getStatus())) {
            throw new AppException("course.delete.notDraft", HttpStatus.CONFLICT);
        }
        courseRepository.delete(course);
    }

    @Override
    public List<CourseToolTabDto> getToolTabs() {
        List<CourseToolTabDto> tabs = new ArrayList<>();
        tabs.add(CourseToolTabDto.builder().value("all").count(courseRepository.count()).build());
        List<MasterData> tools = masterDataRepository.findByTypeAndActiveTrueOrderByPositionAsc(MASTER_TYPE_TOOL);
        for (MasterData t : tools) {
            tabs.add(CourseToolTabDto.builder()
                    .value(t.getCode())
                    .count(courseRepository.countByTool(t.getCode()))
                    .build());
        }
        return tabs;
    }

    @Override
    public CourseStatsDto getStats() {
        long published = courseRepository.countByStatus(STATUS_PUBLISHED);
        long drafts = courseRepository.countByStatus(STATUS_DRAFT);
        long unpublished = courseRepository.countByStatus(STATUS_UNPUBLISHED);
        return CourseStatsDto.builder()
                .total(published + drafts + unpublished)
                .published(published)
                .drafts(drafts)
                .unpublished(unpublished)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PublicCourseDto> listPublicCourses(int page, int size) {
        int clampedSize = Math.max(1, Math.min(size, 50));
        PageRequest pageable = PageRequest.of(Math.max(page - 1, 0), clampedSize, Sort.by(Sort.Direction.DESC, "id"));
        Page<Course> coursePage = courseRepository.search(null, STATUS_PUBLISHED, null, pageable);
        List<PublicCourseDto> data = coursePage.getContent().stream().map(this::toPublicDto).toList();
        return new PageImpl<>(data, pageable, coursePage.getTotalElements());
    }

    @Override
    @Transactional(readOnly = true)
    public PublicCourseDetailDto getPublicCourseById(Long id) {
        Course course = courseDomainService.getCourseById(id);
        // Drafts and unpublished courses are invisible to anonymous callers — return the same
        // 404 as a missing id so we don't leak the existence of internal entities.
        if (!STATUS_PUBLISHED.equals(course.getStatus())) {
            throw new AppException("course.not.found", HttpStatus.NOT_FOUND);
        }
        return toPublicDetailDto(course);
    }

    private PublicCourseDto toPublicDto(Course course) {
        return PublicCourseDto.builder()
                .id(course.getId())
                .code(course.getCode())
                .title(course.getTitle())
                .tagline(course.getTagline())
                .description(course.getDescription())
                .tool(course.getTool())
                .minAge(course.getMinAge())
                .maxAge(course.getMaxAge())
                .totalSessions(course.getTotalSessions())
                .sessionDurationMinutes(course.getSessionDurationMinutes())
                .tuitionAmount(course.getTuitionAmount())
                .originalTuitionAmount(course.getOriginalTuitionAmount())
                .coverUrl(course.getCoverUrl())
                .createdAt(course.getCreatedAt())
                .build();
    }

    private PublicCourseDetailDto toPublicDetailDto(Course course) {
        List<CourseSessionDto> sessionDtos = course.getSessions().stream()
                .sorted(Comparator.comparing(CourseSession::getPosition))
                .map(s -> CourseSessionDto.builder()
                        .title(s.getTitle())
                        .description(s.getDescription())
                        .build())
                .toList();
        return PublicCourseDetailDto.builder()
                .id(course.getId())
                .code(course.getCode())
                .title(course.getTitle())
                .tagline(course.getTagline())
                .description(course.getDescription())
                .tool(course.getTool())
                .minAge(course.getMinAge())
                .maxAge(course.getMaxAge())
                .totalSessions(course.getTotalSessions())
                .sessionDurationMinutes(course.getSessionDurationMinutes())
                .perClassCapacity(course.getPerClassCapacity())
                .tuitionAmount(course.getTuitionAmount())
                .originalTuitionAmount(course.getOriginalTuitionAmount())
                .coverUrl(course.getCoverUrl())
                .introVideoUrl(course.getIntroVideoUrl())
                .pricingNotes(course.getPricingNotes())
                .sessions(sessionDtos)
                .createdAt(course.getCreatedAt())
                .build();
    }

    // ----- helpers -----

    // Update in place rather than clear-then-readd. Re-adding with the same
    // (course_id, position) would race the orphanRemoval DELETE at flush time
    // and trip uq_course_sessions_position.
    private void replaceSessions(Course course, List<CourseSessionInput> inputs) {
        Set<CourseSession> current = course.getSessions();
        if (inputs == null) {
            current.clear();
            return;
        }
        List<CourseSession> sorted = new ArrayList<>(current);
        sorted.sort(Comparator.comparing(CourseSession::getPosition));

        for (int i = sorted.size() - 1; i >= inputs.size(); i--) {
            current.remove(sorted.get(i));
        }

        for (int i = 0; i < inputs.size(); i++) {
            CourseSessionInput s = inputs.get(i);
            String title = s.getTitle().trim();
            String description = trimToNull(s.getDescription());
            if (i < sorted.size()) {
                CourseSession existing = sorted.get(i);
                existing.setPosition(i);
                existing.setTitle(title);
                existing.setDescription(description);
            } else {
                current.add(CourseSession.builder()
                        .course(course)
                        .position(i)
                        .title(title)
                        .description(description)
                        .build());
            }
        }
    }

    private void replaceDiscounts(Course course, List<DiscountRuleInput> inputs) {
        Set<CourseDiscount> current = course.getDiscounts();
        if (inputs == null) {
            current.clear();
            return;
        }
        List<CourseDiscount> sorted = new ArrayList<>(current);
        sorted.sort(Comparator.comparing(CourseDiscount::getPosition));

        for (int i = sorted.size() - 1; i >= inputs.size(); i--) {
            current.remove(sorted.get(i));
        }

        for (int i = 0; i < inputs.size(); i++) {
            DiscountRuleInput d = inputs.get(i);
            BigDecimal numeric = null;
            String text = null;
            if ("special".equals(d.getType())) {
                text = d.getValue() == null ? null : String.valueOf(d.getValue());
            } else {
                numeric = toBigDecimal(d.getValue());
                if (numeric == null) {
                    throw new AppException("course.validation.discount.value.required", HttpStatus.BAD_REQUEST);
                }
            }
            String condition = d.getCondition() == null ? "none" : d.getCondition();
            String name = d.getName().trim();
            if (i < sorted.size()) {
                CourseDiscount existing = sorted.get(i);
                existing.setPosition(i);
                existing.setName(name);
                existing.setType(d.getType());
                existing.setValueNumeric(numeric);
                existing.setValueText(text);
                existing.setCondition(condition);
                existing.setConditionDate(d.getConditionDate());
            } else {
                current.add(CourseDiscount.builder()
                        .course(course)
                        .position(i)
                        .name(name)
                        .type(d.getType())
                        .valueNumeric(numeric)
                        .valueText(text)
                        .condition(condition)
                        .conditionDate(d.getConditionDate())
                        .build());
            }
        }
    }

    private List<DiscountRuleInput> toDiscountInputs(java.util.Collection<CourseDiscount> discounts) {
        List<DiscountRuleInput> list = new ArrayList<>();
        for (CourseDiscount d : discounts) {
            Object value = "special".equals(d.getType()) ? d.getValueText() : d.getValueNumeric();
            list.add(DiscountRuleInput.builder()
                    .name(d.getName())
                    .type(d.getType())
                    .value(value)
                    .condition(d.getCondition())
                    .conditionDate(d.getConditionDate())
                    .build());
        }
        return list;
    }

    private boolean hasNumericDiscount(List<DiscountRuleInput> discounts) {
        if (discounts == null) return false;
        return discounts.stream().anyMatch(d -> "percentage".equals(d.getType()) || "fixed".equals(d.getType()));
    }

    private BigDecimal applyDiscounts(BigDecimal base, List<DiscountRuleInput> discounts) {
        if (base == null) return BigDecimal.ZERO;
        if (discounts == null || discounts.isEmpty()) return base;
        BigDecimal acc = base;
        for (DiscountRuleInput d : discounts) {
            BigDecimal value = toBigDecimal(d.getValue());
            if ("percentage".equals(d.getType()) && value != null) {
                BigDecimal multiplier = BigDecimal.ONE.subtract(value.divide(BigDecimal.valueOf(100), 6, RoundingMode.HALF_UP));
                acc = acc.multiply(multiplier).setScale(0, RoundingMode.HALF_UP);
            } else if ("fixed".equals(d.getType()) && value != null) {
                acc = acc.subtract(value);
            }
            if (acc.signum() < 0) acc = BigDecimal.ZERO;
        }
        return acc;
    }

    private BigDecimal toBigDecimal(Object value) {
        if (value == null) return null;
        if (value instanceof BigDecimal bd) return bd;
        if (value instanceof Number n) return new BigDecimal(n.toString());
        try {
            return new BigDecimal(value.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private String buildTagline(List<String> tags, String fallback) {
        if (tags != null && !tags.isEmpty()) {
            String first = tags.get(0);
            if (first != null && !first.isBlank()) return first.trim();
        }
        return fallback;
    }

    private String bumpVersion(String version) {
        if (version == null) return "v0.2";
        java.util.regex.Matcher m = java.util.regex.Pattern.compile("^v(\\d+)\\.(\\d+)$").matcher(version);
        if (!m.matches()) return "v0.2";
        int major = Integer.parseInt(m.group(1));
        int minor = Integer.parseInt(m.group(2));
        return "v" + major + "." + (minor + 1);
    }

    private CourseDto toDto(Course course) {
        List<CourseSessionDto> sessionDtos = course.getSessions().stream()
                .map(s -> CourseSessionDto.builder()
                        .title(s.getTitle())
                        .description(s.getDescription())
                        .build())
                .toList();

        List<CourseDiscountDto> discountDtos = course.getDiscounts().stream()
                .map(d -> CourseDiscountDto.builder()
                        .name(d.getName())
                        .type(d.getType())
                        .value("special".equals(d.getType()) ? d.getValueText() : d.getValueNumeric())
                        .condition(d.getCondition())
                        .conditionDate(d.getConditionDate())
                        .build())
                .toList();

        return CourseDto.builder()
                .id(course.getId())
                .code(course.getCode())
                .title(course.getTitle())
                .tagline(course.getTagline())
                .description(course.getDescription())
                .tool(course.getTool())
                .status(course.getStatus())
                .minAge(course.getMinAge())
                .maxAge(course.getMaxAge())
                .totalSessions(course.getTotalSessions())
                .sessionDurationMinutes(course.getSessionDurationMinutes())
                .perClassCapacity(course.getPerClassCapacity())
                .capacity(course.getPerClassCapacity())
                .classes(0)
                .enrolled(0)
                .tuitionAmount(course.getTuitionAmount())
                .originalTuitionAmount(course.getOriginalTuitionAmount())
                .coverUrl(course.getCoverUrl())
                .introVideoUrl(course.getIntroVideoUrl())
                .pricingNotes(course.getPricingNotes())
                .version(course.getVersionLabel())
                .curriculum(buildCurriculumPreview(sessionDtos))
                .sessions(sessionDtos)
                .discounts(discountDtos)
                .createdAt(course.getCreatedAt())
                .updatedAt(course.getUpdatedAt())
                .build();
    }

    // Mirror FE buildCurriculum: first 8 session titles plus "… N more sessions" suffix.
    private List<String> buildCurriculumPreview(List<CourseSessionDto> sessions) {
        List<String> preview = new ArrayList<>();
        int cap = Math.min(8, sessions.size());
        for (int i = 0; i < cap; i++) {
            String t = sessions.get(i).getTitle();
            preview.add(t == null || t.isBlank() ? "Session " + (i + 1) : t.trim());
        }
        int remaining = sessions.size() - preview.size();
        if (remaining > 0) {
            preview.add("… " + remaining + " more sessions");
        }
        if (preview.isEmpty()) {
            preview.add("Outline pending");
        }
        return preview;
    }

    private Sort buildSort(String sortBy, String sortDirection) {
        Sort.Direction direction = "asc".equalsIgnoreCase(sortDirection) ? Sort.Direction.ASC : Sort.Direction.DESC;
        if (sortBy == null || sortBy.isBlank()) {
            return Sort.by(direction, "createdAt");
        }
        String field = switch (sortBy) {
            case "createdAt", "updatedAt", "title", "code", "status", "tool", "tuitionAmount" -> sortBy;
            default -> throw new AppException("course.validation.sortBy.invalid", HttpStatus.BAD_REQUEST);
        };
        return Sort.by(direction, field);
    }

    private String normalize(String value) {
        if (value == null) return null;
        String v = value.trim();
        return v.isEmpty() ? null : v;
    }

    private String trimToNull(String value) {
        if (value == null) return null;
        String v = value.trim();
        return v.isEmpty() ? null : v;
    }
}
