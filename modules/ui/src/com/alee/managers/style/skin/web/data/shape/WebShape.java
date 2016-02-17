package com.alee.managers.style.skin.web.data.shape;

import com.alee.laf.grouping.GroupingLayout;
import com.alee.managers.style.skin.web.data.decoration.WebDecoration;
import com.alee.managers.style.skin.web.data.shade.ShadeType;
import com.alee.utils.LafUtils;
import com.alee.utils.ShapeCache;
import com.alee.utils.swing.DataProvider;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.GeneralPath;

/**
 * Most commonnly used shape implementation.
 *
 * @author nsofronov
 * @author Mikle Garin
 */

@XStreamAlias ( "WebShape" )
public class WebShape<E extends JComponent, D extends WebDecoration<E, D>, I extends WebShape<E, D, I>> extends AbstractShape<E, D, I>
{
    /**
     * Decoration corners rounding.
     */
    @XStreamAsAttribute
    protected Integer round;

    /**
     * Displayed decoration sides.
     */
    @XStreamAsAttribute
    protected String sides;

    /**
     * Displayed decoration side lines.
     */
    @XStreamAsAttribute
    protected String lines;

    /**
     * Returns decoration corners rounding.
     *
     * @return decoration corners rounding
     */
    public int getRound ()
    {
        return round != null ? round : 0;
    }

    /**
     * Returns grouping layout used to place specified component if it exists, {@code null} otherwise.
     *
     * @param c painted component
     * @return grouping layout used to place specified component if it exists, {@code null} otherwise
     */
    protected GroupingLayout getGroupingLayout ( final E c )
    {
        final Container parent = c.getParent ();
        if ( parent != null )
        {
            final LayoutManager layout = parent.getLayout ();
            if ( layout instanceof GroupingLayout )
            {
                return ( GroupingLayout ) layout;
            }
        }
        return null;
    }

    /**
     * Returns descriptor for painted component sides.
     *
     * @param c painted component
     * @return descriptor for painted component sides
     */
    protected String getSides ( final E c )
    {
        final GroupingLayout layout = getGroupingLayout ( c );
        return layout != null ? layout.getSides ( c ) : this.sides;
    }

    /**
     * Returns whether or not top side should be painted.
     *
     * @param c painted component
     * @return true if top side should be painted, false otherwise
     */
    public boolean isPaintTop ( final E c )
    {
        final String sides = getSides ( c );
        return sides == null || sides.charAt ( 0 ) != '0';
    }

    /**
     * Returns whether or not left side should be painted.
     *
     * @param c painted component
     * @return true if left side should be painted, false otherwise
     */
    public boolean isPaintLeft ( final E c )
    {
        final String sides = getSides ( c );
        return sides == null || sides.charAt ( 2 ) != '0';
    }

    /**
     * Returns whether or not bottom side should be painted.
     *
     * @param c painted component
     * @return true if bottom side should be painted, false otherwise
     */
    public boolean isPaintBottom ( final E c )
    {
        final String sides = getSides ( c );
        return sides == null || sides.charAt ( 4 ) != '0';
    }

    /**
     * Returns whether or not right side should be painted.
     *
     * @param c painted component
     * @return true if right side should be painted, false otherwise
     */
    public boolean isPaintRight ( final E c )
    {
        final String sides = getSides ( c );
        return sides == null || sides.charAt ( 6 ) != '0';
    }

    /**
     * Returns whether or not any of the sides should be painted.
     *
     * @param c painted component
     * @return true if at least one of the sides should be painted, false otherwise
     */
    public boolean isAnySide ( final E c )
    {
        final String sides = getSides ( c );
        return sides == null || sides.contains ( "1" );
    }

    /**
     * Returns descriptor for painted component lines.
     *
     * @param c painted component
     * @return descriptor for painted component lines
     */
    protected String getLines ( final E c )
    {
        final GroupingLayout layout = getGroupingLayout ( c );
        return layout != null ? layout.getLines ( c ) : this.lines;
    }

    /**
     * Returns whether or not top side line should be painted.
     *
     * @param c painted component
     * @return true if top side line should be painted, false otherwise
     */
    public boolean isPaintTopLine ( final E c )
    {
        final String lines = getLines ( c );
        return !isPaintTop ( c ) && lines != null && lines.charAt ( 0 ) == '1';
    }

    /**
     * Returns whether or not left side line should be painted.
     *
     * @param c painted component
     * @return true if left side line should be painted, false otherwise
     */
    public boolean isPaintLeftLine ( final E c )
    {
        final String lines = getLines ( c );
        return !isPaintLeft ( c ) && lines != null && lines.charAt ( 2 ) == '1';
    }

    /**
     * Returns whether or not bottom side line should be painted.
     *
     * @param c painted component
     * @return true if bottom side line should be painted, false otherwise
     */
    public boolean isPaintBottomLine ( final E c )
    {
        final String lines = getLines ( c );
        return !isPaintBottom ( c ) && lines != null && lines.charAt ( 4 ) == '1';
    }

    /**
     * Returns whether or not right side line should be painted.
     *
     * @param c painted component
     * @return true if right side line should be painted, false otherwise
     */
    public boolean isPaintRightLine ( final E c )
    {
        final String lines = getLines ( c );
        return !isPaintRight ( c ) && lines != null && lines.charAt ( 6 ) == '1';
    }

    /**
     * Returns whether or not any of the side lines should be painted.
     *
     * @param c painted component
     * @return true if at least one of the side lines should be painted, false otherwise
     */
    public boolean isAnyLine ( final E c )
    {
        return isPaintTopLine ( c ) || isPaintLeftLine ( c ) || isPaintBottomLine ( c ) || isPaintRightLine ( c );
    }

    @Override
    public Insets getBorderInsets ( final E c, final D d )
    {
        // Side decorated sides spacing
        final int borderWidth = ( int ) Math.round ( Math.floor ( d.getBorderWidth () ) );
        final int shadeWidth = d.getShadeWidth ( ShadeType.outer );
        final int spacing = shadeWidth + borderWidth;

        // Combining final border insets
        final int top = isPaintTop ( c ) ? spacing : isPaintTopLine ( c ) ? borderWidth : 0;
        final int left = isPaintLeft ( c ) ? spacing : isPaintLeftLine ( c ) ? borderWidth : 0;
        final int bottom = isPaintBottom ( c ) ? spacing : isPaintBottomLine ( c ) ? borderWidth : 0;
        final int right = isPaintRight ( c ) ? spacing : isPaintRightLine ( c ) ? borderWidth : 0;
        return new Insets ( top, left, bottom, right );
    }

    @Override
    public boolean isVisible ( final ShapeType type, final E c, final D d )
    {
        switch ( type )
        {
            case outerShade:
                return isAnySide ( c );

            case border:
                return isAnySide ( c ) || isAnyLine ( c );

            case background:
            case innerShade:
            default:
                return true;
        }
    }

    @Override
    public Shape getShape ( final ShapeType type, final Rectangle bounds, final E c, final D d )
    {
        // Shape settings
        final int round = getRound ();
        final boolean ltr = c.getComponentOrientation ().isLeftToRight ();
        final boolean top = isPaintTop ( c );
        final boolean bottom = isPaintBottom ( c );
        final boolean left = ltr ? isPaintLeft ( c ) : isPaintRight ( c );
        final boolean right = ltr ? isPaintRight ( c ) : isPaintLeft ( c );
        final int sw = d.getShadeWidth ( ShadeType.outer );

        // Retrieving shape
        return ShapeCache.getShape ( c, "WebShape." + type, new DataProvider<Shape> ()
        {
            @Override
            public Shape provide ()
            {
                final int x = bounds.x;
                final int y = bounds.y;
                final int w = bounds.width;
                final int h = bounds.height;

                if ( type != ShapeType.border )
                {
                    final int shear = type != ShapeType.background ? -1 : 0;

                    final Point[] corners = new Point[ 4 ];
                    final boolean[] rounded = new boolean[ 4 ];

                    corners[ 0 ] = new Point ( x + ( left ? sw : 0 ), y + ( top ? sw : 0 ) );
                    rounded[ 0 ] = left && top;

                    corners[ 1 ] = new Point ( x + ( right ? w - sw : w ) + shear, y + ( top ? sw : 0 ) );
                    rounded[ 1 ] = right && top;

                    corners[ 2 ] = new Point ( x + ( right ? w - sw : w ) + shear, y + ( bottom ? h - sw : h ) + shear );
                    rounded[ 2 ] = right && bottom;

                    corners[ 3 ] = new Point ( x + ( left ? sw : 0 ), y + ( bottom ? h - sw : h ) + shear );
                    rounded[ 3 ] = left && bottom;

                    return LafUtils.createRoundedShape ( round > 0 ? round + 1 : 0, corners, rounded );
                }
                else
                {
                    final GeneralPath shape = new GeneralPath ( GeneralPath.WIND_EVEN_ODD );
                    boolean connect;
                    boolean moved = false;
                    if ( top )
                    {
                        shape.moveTo ( x + ( left ? sw + round : 0 ), y + sw );
                        if ( right )
                        {
                            shape.lineTo ( x + w - sw - round - 1, y + sw );
                            shape.quadTo ( x + w - sw - 1, y + sw, x + w - sw - 1, y + sw + round );
                        }
                        else
                        {
                            shape.lineTo ( x + w - 1, y + sw );
                        }
                        connect = true;
                    }
                    else
                    {
                        connect = false;
                    }
                    if ( right )
                    {
                        if ( !connect )
                        {
                            shape.moveTo ( x + w - sw - 1, y + ( top ? sw + round : 0 ) );
                            moved = true;
                        }
                        if ( bottom )
                        {
                            shape.lineTo ( x + w - sw - 1, y + h - sw - round - 1 );
                            shape.quadTo ( x + w - sw - 1, y + h - sw - 1, x + w - sw - round - 1, y + h - sw - 1 );
                        }
                        else
                        {
                            shape.lineTo ( x + w - sw - 1, y + h - 1 );
                        }
                        connect = true;
                    }
                    else
                    {
                        connect = false;
                    }
                    if ( bottom )
                    {
                        if ( !connect )
                        {
                            shape.moveTo ( x + w + ( right ? -sw - round - 1 : -1 ), y + h - sw - 1 );
                            moved = true;
                        }
                        if ( left )
                        {
                            shape.lineTo ( x + sw + round, y + h - sw - 1 );
                            shape.quadTo ( x + sw, y + h - sw - 1, x + sw, y + h - sw - round - 1 );
                        }
                        else
                        {
                            shape.lineTo ( x, y + h - sw - 1 );
                        }
                        connect = true;
                    }
                    else
                    {
                        connect = false;
                    }
                    if ( left )
                    {
                        if ( !connect )
                        {
                            shape.moveTo ( x + sw, y + h + ( bottom ? -sw - round - 1 : -1 ) );
                            moved = true;
                        }
                        if ( top )
                        {
                            shape.lineTo ( x + sw, y + sw + round );
                            shape.quadTo ( x + sw, y + sw, x + sw + round, y + sw );
                            if ( !moved )
                            {
                                shape.closePath ();
                            }
                        }
                        else
                        {
                            shape.lineTo ( x + sw, y );
                        }
                    }
                    return shape;
                }
            }
        }, bounds, sw, round, top, bottom, left, right );
    }

    @Override
    public I merge ( final I shape )
    {
        if ( shape.round != null )
        {
            round = shape.round;
        }
        if ( shape.sides != null )
        {
            sides = shape.sides;
        }
        if ( shape.lines != null )
        {
            lines = shape.lines;
        }
        return ( I ) this;
    }
}