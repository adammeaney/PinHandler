package com.ameaney.pinhandler;

public class Position
{
    private double getX()
    {
        return X;
    }

    private double X;

    private void setX(double x)
    {
        X = x;
    }

    private double getY()
    {
        return Y;
    }

    private double Y;

    private void setY(double y)
    {
        Y = y;
    }

    public Position()
    {
        this(0, 0);
    }

    public Position(double x, double y)
    {
        X = x;
        Y = y;
    }

    public double getDisplacement(Position other)
    {
        if (other == null)
        {
            return Double.MAX_VALUE;
        }

        if (other.getX() < 0 || other.getY() < 0 || X < 0 || Y < 0)
        {
            return Double.MAX_VALUE;
        }

        double deltaX = Math.abs(other.getX() - X);
        double deltaY = Math.abs(other.getY() - Y);

        return Math.sqrt(deltaX * deltaX + deltaY * deltaY);
    }
}
