/*******************************************************************************
 * Copyright (C) 2014 John Casey.
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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.commonjava.maven.atlas.graph.filter;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.commonjava.maven.atlas.graph.rel.ProjectRelationship;
import org.commonjava.maven.atlas.graph.rel.RelationshipType;

public class AnyFilter
    implements ProjectRelationshipFilter
{

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public static final AnyFilter INSTANCE = new AnyFilter();

    private AnyFilter()
    {
    }

    @Override
    public boolean accept( final ProjectRelationship<?> rel )
    {
        return true;
    }

    @Override
    public ProjectRelationshipFilter getChildFilter( final ProjectRelationship<?> parent )
    {
        return this;
    }

    @Override
    public String getLongId()
    {
        return "ANY";
    }

    @Override
    public String toString()
    {
        return getLongId();
    }

    @Override
    public boolean equals( final Object obj )
    {
        return obj instanceof AnyFilter;
    }

    @Override
    public int hashCode()
    {
        return AnyFilter.class.hashCode() + 1;
    }

    @Override
    public String getCondensedId()
    {
        return getLongId();
    }

    @Override
    public boolean includeManagedRelationships()
    {
        return true;
    }

    @Override
    public boolean includeConcreteRelationships()
    {
        return true;
    }

    @Override
    public Set<RelationshipType> getAllowedTypes()
    {
        return new HashSet<RelationshipType>( Arrays.asList( RelationshipType.values() ) );
    }

}
