/*
 * This file is part of WebLookAndFeel library.
 *
 * WebLookAndFeel library is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * WebLookAndFeel library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with WebLookAndFeel library.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.alee.extended.menu;

import com.alee.extended.image.WebImage;
import com.alee.global.StyleConstants;
import com.alee.laf.panel.WebPanel;
import com.alee.laf.rootpane.WebWindow;
import com.alee.utils.GeometryUtils;
import com.alee.utils.GraphicsUtils;
import com.alee.utils.SwingUtils;
import com.alee.utils.swing.WebTimer;
import com.alee.utils.swing.WindowFollowAdapter;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Custom dynamic menu with pretty display/hide animations.
 *
 * @author Mikle Garin
 */

public class WebDynamicMenu extends WebPanel
{
    /**
     * todo 1. Allow sliding down vertical menu (with text and selection background)
     */

    /**
     * Radius of the menu background in px.
     */
    protected int radius;

    /**
     * First element position angle in degrees.
     * Counted from the circle top-most point.
     */
    protected double startingAngle;

    /**
     * Angle which is available for menu elements.
     * It is 360 degress by default (whole circle available).
     */
    protected double angleRange;

    /**
     * Single animation step progress.
     * Making this value bigger will speedup the animation, reduce required resources but will also make it less soft.
     */
    protected float stepProgress;

    /**
     * Menu animation type.
     */
    protected DynamicMenuType type;

    /**
     * Menu hide animation type.
     */
    protected DynamicMenuType hideType;

    /**
     * Menu animation direction.
     */
    protected boolean clockwise;

    /**
     * Menu items list.
     */
    protected List<WebDynamicMenuItem> items = new ArrayList<WebDynamicMenuItem> ();

    /**
     * Menu display progress.
     */
    protected float currentProgress = 0f;

    /**
     * Actions synchronization object.
     */
    protected final Object sync = new Object ();

    /**
     * Animation timer.
     */
    protected WebTimer animator = null;

    /**
     * Window in which menu is currently displayed.
     * It is null in case menu is currently hidden.
     */
    protected WebWindow window;

    /**
     * Invoker component window.
     */
    protected Window invokerWindow;

    /**
     * Invoker window follow adapter.
     */
    protected WindowFollowAdapter followAdapter;

    /**
     * Whether menu is being displayed or not.
     */
    protected boolean displaying = false;

    /**
     * Whether menu is being hidden or not.
     */
    protected boolean hiding = false;

    /**
     * Index of menu item that caused menu to close.
     * This might affect the hiding animation.
     */
    protected int hidingCause = -1;

    /**
     * Custom global mouse listener that closes menu.
     */
    protected AWTEventListener closeListener;

    /**
     * Listeners synchronization object.
     */
    protected final Object lsync = new Object ();

    /**
     * Actions to perform on full display.
     */
    protected final List<Runnable> onFullDisplay = new ArrayList<Runnable> ();

    /**
     * Actions to perform on full hide.
     */
    protected final List<Runnable> onFullHide = new ArrayList<Runnable> ();

    /**
     * Constructs new dynamic menu.
     */
    public WebDynamicMenu ()
    {
        super ( "transparent", new DynamicMenuLayout () );
        setRadius ( 60 );
        setStartingAngle ( 0 );
        setAngleRange ( 360 );
        setStepProgress ( 0.04f );
        setType ( DynamicMenuType.shutter );
        setHideType ( null );
        setClockwise ( true );
    }

    public int getRadius ()
    {
        return radius;
    }

    public void setRadius ( final int radius )
    {
        this.radius = radius;
    }

    public double getStartingAngle ()
    {
        return startingAngle;
    }

    public void setStartingAngle ( final double startingAngle )
    {
        this.startingAngle = startingAngle;
    }

    public double getAngleRange ()
    {
        return angleRange;
    }

    public void setAngleRange ( final double angleRange )
    {
        this.angleRange = angleRange;
    }

    public float getStepProgress ()
    {
        return stepProgress;
    }

    public void setStepProgress ( final float stepProgress )
    {
        this.stepProgress = stepProgress;
    }

    public DynamicMenuType getType ()
    {
        return type;
    }

    public void setType ( final DynamicMenuType type )
    {
        this.type = type;
    }

    public DynamicMenuType getHideType ()
    {
        return hideType != null ? hideType : type;
    }

    public void setHideType ( final DynamicMenuType hideType )
    {
        this.hideType = hideType;
    }

    public boolean isClockwise ()
    {
        return clockwise;
    }

    public void setClockwise ( final boolean clockwise )
    {
        this.clockwise = clockwise;
    }

    public List<WebDynamicMenuItem> getItems ()
    {
        return items;
    }

    public WebImage addItem ( final ImageIcon icon )
    {
        return addItem ( icon, null );
    }

    public WebImage addItem ( final ImageIcon icon, final ActionListener action )
    {
        return addItem ( new WebDynamicMenuItem ( icon, action ) );
    }

    public WebImage addItem ( final WebDynamicMenuItem item )
    {
        items.add ( item );

        final WebImage menuItem = new WebImage ( item.getIcon () )
        {
            @Override
            protected void paintComponent ( final Graphics g )
            {
                if ( item.isDrawBorder () )
                {
                    final Graphics2D g2d = ( Graphics2D ) g;
                    final Object aa = GraphicsUtils.setupAntialias ( g2d );
                    g2d.setPaint ( isEnabled () ? item.getBorderColor () : item.getDisabledBorderColor () );
                    g2d.fillOval ( 0, 0, getWidth (), getHeight () );
                    g2d.setColor ( Color.WHITE );
                    g2d.fillOval ( 2, 2, getWidth () - 4, getHeight () - 4 );
                    GraphicsUtils.restoreAntialias ( g2d, aa );
                }

                super.paintComponent ( g );
            }
        };
        menuItem.setEnabled ( item.getAction () != null );
        menuItem.setMargin ( item.getMargin () );

        menuItem.addMouseListener ( new MouseAdapter ()
        {
            @Override
            public void mousePressed ( final MouseEvent e )
            {
                final ActionListener action = item.getAction ();
                if ( action != null )
                {
                    action.actionPerformed ( new ActionEvent ( e.getSource (), 0, "Action performed" ) );
                }
                hideMenu ( getComponentZOrder ( menuItem ) );
            }
        } );
        add ( menuItem );

        return menuItem;
    }

    public float getCurrentProgress ()
    {
        return currentProgress;
    }

    public WebTimer getAnimator ()
    {
        return animator;
    }

    public boolean isDisplaying ()
    {
        return displaying;
    }

    public boolean isHiding ()
    {
        return hiding;
    }

    /**
     * Returns index of menu item that caused menu to close.
     *
     * @return index of menu item that caused menu to close
     */
    public int getHidingCause ()
    {
        return hidingCause;
    }

    /**
     * Displays dynamic menu for the specified invoker location.
     *
     * @param invoker  menu invoker
     * @param location menu location
     */
    public void showMenu ( final Component invoker, final Point location )
    {
        synchronized ( sync )
        {
            if ( displaying || window != null || getComponentCount () == 0 )
            {
                return;
            }

            displaying = true;

            // Stop displaying if we were
            if ( hiding )
            {
                if ( animator != null )
                {
                    animator.stop ();
                }
                hiding = false;
                hidingCause = -1;
            }

            // Creating menu and displaying it
            createMenuWindow ( invoker, location );
            window.setVisible ( true );

            // Displaying menu softly
            animator = WebTimer.repeat ( StyleConstants.fastAnimationDelay, 0L, new ActionListener ()
            {
                @Override
                public void actionPerformed ( final ActionEvent e )
                {
                    synchronized ( sync )
                    {
                        if ( currentProgress < 1f )
                        {
                            currentProgress = Math.min ( currentProgress + stepProgress, 1f );
                            revalidate ();
                            window.setWindowOpacity ( currentProgress );
                        }
                        else
                        {
                            animator.stop ();
                            animator = null;
                            displaying = false;
                            fullyDisplayed ();
                        }
                    }
                }
            } );
        }
    }

    /**
     * Hides dynamic menu.
     */
    public void hideMenu ()
    {
        hideMenu ( -1 );
    }

    /**
     * Hides dynamic menu.
     *
     * @param index menu item that forced menu to hide
     */
    public void hideMenu ( final int index )
    {
        synchronized ( sync )
        {
            if ( hiding )
            {
                return;
            }

            hiding = true;
            hidingCause = index;

            // Stop displaying if we were
            if ( displaying )
            {
                if ( animator != null )
                {
                    animator.stop ();
                }
                displaying = false;
            }

            // Hiding menu softly
            animator = WebTimer.repeat ( StyleConstants.fastAnimationDelay, 0L, new ActionListener ()
            {
                @Override
                public void actionPerformed ( final ActionEvent e )
                {
                    synchronized ( sync )
                    {
                        if ( currentProgress > 0f )
                        {
                            currentProgress = Math.max ( currentProgress - stepProgress, 0f );
                            revalidate ();
                            window.setWindowOpacity ( currentProgress );
                        }
                        else
                        {
                            cleanupMenuWindow ();
                            animator.stop ();
                            animator = null;
                            hiding = false;
                            hidingCause = -1;
                            fullyHidden ();
                        }
                    }
                }
            } );
        }
    }

    /**
     * Performs provided action when menu is fully displayed.
     * Might be useful to display sub-menus or perform some other actions.
     * Be aware that this action will be performed only once and then removed from the actions list.
     *
     * @param action action to perform
     */
    public void onFullDisplay ( final Runnable action )
    {
        synchronized ( lsync )
        {
            if ( !isShowing () || isDisplaying () )
            {
                onFullDisplay.add ( action );
            }
            else if ( isShowing () && !isHiding () )
            {
                action.run ();
            }
        }
    }

    /**
     * Performs actions waiting for menu display animation finish.
     */
    public void fullyDisplayed ()
    {
        synchronized ( lsync )
        {
            for ( final Runnable runnable : onFullDisplay )
            {
                runnable.run ();
            }
            onFullDisplay.clear ();
        }
    }

    /**
     * Performs provided action when menu is fully hidden.
     * Be aware that this action will be performed only once and then removed from the actions list.
     *
     * @param action action to perform
     */
    public void onFullHide ( final Runnable action )
    {
        synchronized ( lsync )
        {
            if ( isShowing () )
            {
                onFullHide.add ( action );
            }
            else
            {
                action.run ();
            }
        }
    }

    /**
     * Performs actions waiting for menu hide animation finish.
     */
    public void fullyHidden ()
    {
        synchronized ( lsync )
        {
            for ( final Runnable runnable : onFullHide )
            {
                runnable.run ();
            }
            onFullHide.clear ();
        }
    }

    /**
     * Returns menu item angle relative to vertical axis.
     *
     * @param item menu item
     * @return menu item center point angle
     */
    public double getItemAngle ( final Component item )
    {
        return getItemAngle ( getComponentZOrder ( item ) );
    }

    /**
     * Returns menu item angle relative to vertical axis.
     *
     * @param index menu item index
     * @return menu item center point angle
     */
    public double getItemAngle ( final int index )
    {
        return GeometryUtils.toDegrees ( getActualLayout ().getItemAngle ( this, index ) );
    }

    /**
     * Returns actual menu layout manager.
     *
     * @return actual menu layout manager
     */
    public DynamicMenuLayout getActualLayout ()
    {
        return ( DynamicMenuLayout ) getLayout ();
    }

    /**
     * Creates new menu window.
     *
     * @param invoker  menu invoker
     * @param location menu location
     */
    protected void createMenuWindow ( final Component invoker, final Point location )
    {
        // Creating window with menu as content
        window = new WebWindow ( invoker );
        window.setContentPane ( this );

        // Making window non-opaque and transparent
        window.setWindowOpaque ( false );
        window.setWindowOpacity ( currentProgress );

        // Packing to preferred size
        window.pack ();

        // Applying proper position
        final Rectangle bos = SwingUtils.getBoundsOnScreen ( invoker );
        window.setLocation ( bos.x + location.x - getWidth () / 2, bos.y + location.y - getHeight () / 2 );

        // Window following behavior
        invokerWindow = SwingUtils.getWindowAncestor ( invoker );
        followAdapter = WindowFollowAdapter.install ( window, invokerWindow );

        // Creating menu hide event listener
        closeListener = new AWTEventListener ()
        {
            @Override
            public void eventDispatched ( final AWTEvent event )
            {
                final MouseEvent e = ( MouseEvent ) event;
                if ( e.getID () == MouseEvent.MOUSE_PRESSED )
                {
                    final Component component = e.getComponent ();
                    if ( SwingUtils.getWindowAncestor ( component ) != window )
                    {
                        hideMenu ();
                    }
                }
            }
        };
        Toolkit.getDefaultToolkit ().addAWTEventListener ( closeListener, AWTEvent.MOUSE_EVENT_MASK );
    }

    /**
     * Disposes old menu window.
     */
    protected void cleanupMenuWindow ()
    {
        // Removing menu hide event listener
        Toolkit.getDefaultToolkit ().removeAWTEventListener ( closeListener );
        closeListener = null;

        // Removing follow adapter
        WindowFollowAdapter.uninstall ( invokerWindow, followAdapter );
        invokerWindow = null;
        followAdapter = null;

        // Disposing of menu window
        window.dispose ();
        window = null;
    }

    /**
     * Returns whether any dynamic menu is currently displayed or not.
     *
     * @return true if any dynamic menu is currently displayed, false otherwise
     */
    public static boolean isAnyDynamicMenuDisplayed ()
    {
        for ( final Window window : Window.getWindows () )
        {
            if ( window.isShowing () && window instanceof WebWindow &&
                    ( ( WebWindow ) window ).getContentPane () instanceof WebDynamicMenu )
            {
                return true;
            }
        }
        return false;
    }

    /**
     * Hides all visible dynamic menus.
     */
    public static void hideAllDynamicMenus ()
    {
        for ( final Window window : Window.getWindows () )
        {
            if ( window.isShowing () && window instanceof WebWindow &&
                    ( ( WebWindow ) window ).getContentPane () instanceof WebDynamicMenu )
            {
                final WebDynamicMenu menu = ( WebDynamicMenu ) ( ( WebWindow ) window ).getContentPane ();
                menu.hideMenu ();
            }
        }
    }
}