// ruta: src/main/java/com/upsjb/ms4/specification/SpecificationSupport.java
package com.upsjb.ms4.specification;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

abstract class SpecificationSupport {

    protected static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    protected static Predicate and(CriteriaBuilder cb, List<Predicate> predicates) {
        return predicates == null || predicates.isEmpty()
                ? cb.conjunction()
                : cb.and(predicates.toArray(Predicate[]::new));
    }

    protected static void addEqual(List<Predicate> predicates,
                                   CriteriaBuilder cb,
                                   Path<?> path,
                                   Object value) {
        if (value != null) {
            predicates.add(cb.equal(path, value));
        }
    }

    protected static void addEqualIgnoreCase(List<Predicate> predicates,
                                             CriteriaBuilder cb,
                                             Expression<String> expression,
                                             String value) {
        if (hasText(value)) {
            predicates.add(cb.equal(cb.lower(expression), value.trim().toLowerCase(Locale.ROOT)));
        }
    }

    protected static void addLike(List<Predicate> predicates,
                                  CriteriaBuilder cb,
                                  Expression<String> expression,
                                  String value) {
        if (hasText(value)) {
            predicates.add(cb.like(cb.lower(expression), contains(value)));
        }
    }

    @SafeVarargs
    protected static void addLikeAny(List<Predicate> predicates,
                                     CriteriaBuilder cb,
                                     String value,
                                     Expression<String>... expressions) {
        if (!hasText(value) || expressions == null || expressions.length == 0) {
            return;
        }

        List<Predicate> likes = new ArrayList<>();

        for (Expression<String> expression : expressions) {
            if (expression != null) {
                likes.add(cb.like(cb.lower(expression), contains(value)));
            }
        }

        if (!likes.isEmpty()) {
            predicates.add(cb.or(likes.toArray(Predicate[]::new)));
        }
    }

    protected static <T extends Comparable<? super T>> void addRange(List<Predicate> predicates,
                                                                     CriteriaBuilder cb,
                                                                     Path<T> path,
                                                                     T from,
                                                                     T to) {
        if (from != null) {
            predicates.add(cb.greaterThanOrEqualTo(path, from));
        }

        if (to != null) {
            predicates.add(cb.lessThanOrEqualTo(path, to));
        }
    }

    protected static String contains(String value) {
        return "%" + value.trim().toLowerCase(Locale.ROOT) + "%";
    }
}