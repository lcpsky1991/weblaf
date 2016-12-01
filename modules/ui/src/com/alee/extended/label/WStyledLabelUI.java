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

package com.alee.extended.label;

import com.alee.laf.label.WLabelUI;

import javax.swing.*;

/**
 * Pluggable look and feel interface for {@link WebStyledLabel} component.
 *
 * @author Mikle Garin
 * @see <a href="https://github.com/mgarin/weblaf/wiki/How-to-use-WebStyledLabel">How to use WebStyledLabel</a>
 * @see WebStyledLabel
 */

public abstract class WStyledLabelUI extends WLabelUI implements SwingConstants
{
    /**
     * Runtime variables.
     */
    protected WebStyledLabel label;

    @Override
    public void installUI ( final JComponent c )
    {
        // Saving label reference
        label = ( WebStyledLabel ) c;

        super.installUI ( c );
    }

    @Override
    public void uninstallUI ( final JComponent c )
    {
        super.uninstallUI ( c );

        // Removing label reference
        label = null;
    }

    @Override
    protected String getFontKey ()
    {
        return "StyledLabel.font";
    }

    @Override
    protected void installDefaults ()
    {
        super.installDefaults ();

        label.setWrap ( TextWrap.mixed );
        label.setHorizontalTextAlignment ( -1 );
        label.setVerticalTextAlignment ( CENTER );
        label.setRows ( 0 );
        label.setMinimumRows ( 0 );
        label.setMaximumRows ( 0 );
    }
}