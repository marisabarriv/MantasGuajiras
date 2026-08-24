package com.mantasguajiras.backend.common.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Constants {

    public static final String DEFAULT_PAGE = "0";

    public static final String DEFAULT_SIZE = "10";

    public static final String DEFAULT_SORT_DIRECTION = "ASC";

    public static final int MIN_PAGE_SIZE = 1;

    public static final int MAX_PAGE_SIZE = 100;
}