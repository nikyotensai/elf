/*
 * Copyright (C) 2019 Qunar, Inc.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.github.nikyotensai.decompiler.jdk827;

import java.util.Iterator;
import java.util.Objects;
import java.util.Set;

import com.google.common.base.Predicate;

/**
 * @author leix.xie
 * @since 2019/4/18 14:13
 */
public class Set827 {

    public static <T> boolean removeIf(Set<T> set, Predicate<? super T> filter) {
        Objects.requireNonNull(filter);
        boolean removed = false;
        final Iterator<T> each = set.iterator();
        while (each.hasNext()) {
            if (filter.apply(each.next())) {
                each.remove();
                removed = true;
            }
        }
        return removed;
    }
}
