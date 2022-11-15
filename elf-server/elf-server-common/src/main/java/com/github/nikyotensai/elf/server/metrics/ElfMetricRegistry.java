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

package com.github.nikyotensai.elf.server.metrics;

import com.google.common.base.Supplier;

/**
 * @author leix.xie
 * @since 2019/7/8 15:10
 */
public interface ElfMetricRegistry {

    void newGauge(final String name, final String[] tags, final String[] values, final Supplier<Double> supplier);

    ElfCounter newCounter(final String name, final String[] tags, final String[] values);

    ElfMeter newMeter(final String name, final String[] tags, final String[] values);

    ElfTimer newTimer(final String name, final String[] tags, final String[] values);

    void remove(final String name, final String[] tags, final String[] values);
}
