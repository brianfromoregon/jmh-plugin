package com.brianfromoregon;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Ordering;

import java.text.DecimalFormat;

public enum SubSecond {
    ns(1.0, "ns"), μs(1.0 / 1e3, "μs"), ms(1.0 / 1e6, "ms"), s(1.0 / 1e9, "s");

    public final double scalar;
    public final String name;

    private SubSecond(double scalar, String name) {
        this.scalar = scalar;
        this.name = name;
    }

    public double fromNanos(Number nanos) {
        return nanos.doubleValue() * scalar;
    }

    public String format(Number nanos, DecimalFormat fmt) {
        return fmt.format(fromNanos(nanos)) + name;
    }

    public String format(Number nanos) {
        return format(nanos, new DecimalFormat("0.0"));
    }

    public static SubSecond finestFor(Number nanos) {
        double step = nanos.doubleValue();
        int n = 0;
        for (; step >= 1000 && n < 3; n++)
            step /= 1000d;

        switch (n) {
            case 0:
                return SubSecond.ns;
            case 1:
                return SubSecond.μs;
            case 2:
                return SubSecond.ms;
            default:
                return SubSecond.s;
        }
    }

    public static SubSecond finestFor(Iterable<? extends Number> nanos) {
        return Ordering.natural().min(Iterables.transform(nanos, new Function<Number, SubSecond>() {
            @Override
            public SubSecond apply(Number num) {
                return SubSecond.finestFor(num);
            }
        }));
    }
}
