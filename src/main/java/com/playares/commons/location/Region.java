package com.playares.commons.location;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public interface Region {
    /**
     * Returns the UUID for this region
     * @return UUID
     */
    UUID getUniqueId();

    /**
     * Returns corner X
     * @return Coordinate X
     */
    double getX1();

    /**
     * Returns opposite corner X
     * @return Coordinate X
     */
    double getX2();

    /**
     * Returns corner Y
     * @return Coordinate Y
     */
    double getY1();

    /**
     * Returns opposite corner Y
     * @return Y Coordinate
     */
    double getY2();

    /**
     * Returns corner Z
     * @return Coordinate Z
     */
    double getZ1();

    /**
     * Returns opposite corner Z
     * @return Coordinate Z
     */
    double getZ2();

    /**
     * Returns location world name
     * @return World Name
     */
    String getWorldName();

    /**
     * Returns a BLocatable containing a corner coordinate of this region
     * @param corner Corner ID (1-4)
     * @return BLocatable
     */
    default BLocatable getCorner(int corner) {
        if (corner < 1 || corner > 5) {
            throw new ArrayIndexOutOfBoundsException("Corner out of bounds - Must be 1-4");
        }

        if (corner == 1) {
            return new BLocatable(getWorldName(), getX1(), 64.0, getZ1());
        }

        if (corner == 2) {
            return new BLocatable(getWorldName(), getX2(), 64.0, getZ1());
        }

        if (corner == 3) {
            return new BLocatable(getWorldName(), getX1(), 64.0, getZ2());
        }

        if (corner == 4) {
            return new BLocatable(getWorldName(), getX2(), 64.0, getZ2());
        }

        return null;
    }

    /**
     * Returns an array[2] containing the length and width of this region
     * @return arr[L x W]
     */
    default int[] getLxW() {
        final int[] result = new int[2];

        final double xMin = Math.min(getX1(), getX2());
        final double zMin = Math.min(getZ1(), getZ2());
        final double xMax = Math.max(getX1(), getX2());
        final double zMax = Math.max(getZ1(), getZ2());

        result[0] = (int)Math.round(Math.abs(xMax - xMin));
        result[1] = (int)Math.round(Math.abs(zMax - zMin));

        return result;
    }

    /**
     * Returns true if the provided coordinates intersect this region
     * @param x1 X1
     * @param z1 Z1
     * @param x2 X2
     * @param z2 Z2
     * @param world World Name
     * @return True if intersects
     */
    default boolean overlaps(double x1, double z1, double x2, double z2, String world) {
        if (!getWorldName().equals(world)) {
            return false;
        }

        final double[] values = new double[2];

        final double xMin = Math.min(getX1(), getX2());
        final double zMin = Math.min(getZ1(), getZ2());
        final double xMax = Math.max(getX1(), getX2());
        final double zMax = Math.max(getZ1(), getZ2());

        values[0] = x1;
        values[1] = x2;
        Arrays.sort(values);

        if (xMin > values[1] || xMax < values[0]) {
            return false;
        }

        values[0] = z1;
        values[1] = z2;
        Arrays.sort(values);

        if (zMin > values[1] || zMax < values[0]) {
            return false;
        }

        return true;
    }

    /**
     * Returns true if the provided location is inside this region
     * @param location Location
     * @param ignoreY If true, will not check if it under or above this region
     * @return True if inside
     */
    default boolean inside(Locatable location, boolean ignoreY) {
        if (!location.getWorldName().equals(getWorldName())) {
            return false;
        }

        double xMin = Math.min(getX1(), getX2());
        double yMin = Math.min(getY1(), getY2());
        double zMin = Math.min(getZ1(), getZ2());
        double xMax = Math.max(getX1(), getX2());
        double yMax = Math.max(getY1(), getY2());
        double zMax = Math.max(getZ1(), getZ2());

        if (location instanceof PLocatable) {
            xMax++;
            zMax++;
        }

        if (ignoreY) {
            return (location.getX() >= xMin && location.getX() <= xMax) &&
                    (location.getZ() >= zMin && location.getZ() <= zMax);
        }

        return
                (location.getX() >= xMin && location.getX() <= xMax) &&
                        (location.getY() >= yMin && location.getY() <= yMax) &&
                        (location.getZ() >= zMin && location.getZ() <= zMax);
    }

    /**
     * Returns an Immutable List of block locations on the perimeter of this region on the provided Y
     * @param y Y to search on
     * @return Immutable List of BLocatables
     */
    default ImmutableList<BLocatable> getPerimeter(int y) {
        final List<BLocatable> locations = Lists.newArrayList();

        final double xMin = Math.min(getX1(), getX2());
        final double zMin = Math.min(getZ1(), getZ2());
        final double xMax = Math.max(getX1(), getX2());
        final double zMax = Math.max(getZ1(), getX2());

        for (int x = (int)xMin; x <= (int)xMax; x++) {
            for (int z = (int)zMin; z <= (int)zMax; z++) {
                if (x == xMin || x == xMax || z == zMin || z == zMax) {
                    locations.add(new BLocatable(getWorldName(), x, y, z));
                }
            }
        }

        return ImmutableList.copyOf(locations);
    }

    /**
     * Extremely taxing method to get all blocks inside a region
     * @return ImmutableList of BLocatables
     */
    default ImmutableList<BLocatable> getBlocks() {
        final List<BLocatable> locations = Lists.newArrayList();

        double xMin = Math.min(getX1(), getX2());
        double yMin = Math.min(getY1(), getY2());
        double zMin = Math.min(getZ1(), getZ2());
        double xMax = Math.max(getX1(), getX2());
        double yMax = Math.max(getY1(), getY2());
        double zMax = Math.max(getZ1(), getZ2());

        for (int x = (int)xMin; x <= (int)xMax; x++) {
            for (int y = (int)yMin; y <= (int)yMax; y++) {
                for (int z = (int)zMin; z <= (int)zMax; z++) {
                    locations.add(new BLocatable(getWorldName(), x, y, z));
                }
            }
        }

        return ImmutableList.copyOf(locations);
    }
}