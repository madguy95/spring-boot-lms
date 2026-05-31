package com.springjwt.module.teacher.model.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MyClassesRequest {
    /**
     * Optional status filter: running | upcoming | ended. Null / "all" returns
     * every class the teacher owns. Unknown values are treated as "all".
     */
    private String status;
}
