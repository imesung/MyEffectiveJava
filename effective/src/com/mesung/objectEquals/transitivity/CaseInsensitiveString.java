package com.mesung.objectEquals.transitivity;

import java.util.Objects;

public final class CaseInsensitiveString {
    private final String s;

    public CaseInsensitiveString(String s) {
       this.s = Objects.requireNonNull(s);
    }

    public boolean equals(Object o) {
        if(o instanceof CaseInsensitiveString) {
            return s.equalsIgnoreCase(((CaseInsensitiveString) o).s);
        }

        if(o instanceof String) {
            return s.equalsIgnoreCase((String) o);
        }
        return false;
    }
}
