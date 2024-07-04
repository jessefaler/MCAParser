package com.protoxon.mca;

public class Pos {

    int z;

    int x;

    Pos(int x, int z) {
        this.x = x;
        this.z = z;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Pos other = (Pos) o;

        // Compare x and z values
        if (x != other.x) return false;
        return z == other.z;
    }

    @Override
    public int hashCode() {
        // Generate hash code based on x and z
        int result = x;
        result = 31 * result + z;
        return result;
    }
}
