/*******************************************************************************
 * Copyright (C) 2013 John Casey.
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
package org.commonjava.maven.atlas.graph.traverse;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.commonjava.maven.atlas.graph.filter.OrFilter;
import org.commonjava.maven.atlas.graph.filter.ParentFilter;
import org.commonjava.maven.atlas.graph.filter.ProjectRelationshipFilter;
import org.commonjava.maven.atlas.graph.model.EProjectCycle;
import org.commonjava.maven.atlas.graph.model.EProjectNet;
import org.commonjava.maven.atlas.graph.rel.ProjectRelationship;
import org.commonjava.maven.atlas.graph.spi.GraphDriverException;
import org.commonjava.maven.atlas.graph.traverse.model.BuildOrder;
import org.commonjava.maven.atlas.ident.ref.ArtifactRef;
import org.commonjava.maven.atlas.ident.ref.ProjectRef;
import org.commonjava.maven.atlas.ident.ref.ProjectVersionRef;

public class BuildOrderTraversal
    extends AbstractFilteringTraversal
{

    private final List<ProjectRef> order = new ArrayList<ProjectRef>();

    private Set<EProjectCycle> cycles;

    public BuildOrderTraversal()
    {
    }

    public BuildOrderTraversal( final ProjectRelationshipFilter filter )
    {
        super( new OrFilter( filter, new ParentFilter( false ) ) );
    }

    public BuildOrder getBuildOrder( final EProjectNet network )
    {
        detectCycles( network );
        return new BuildOrder( order, cycles );
    }

    private void detectCycles( final EProjectNet network )
    {
        Set<EProjectCycle> cycles = network.getCycles();
        if ( cycles != null )
        {
            cycles = new HashSet<EProjectCycle>( cycles );
            for ( final Iterator<EProjectCycle> iterator = cycles.iterator(); iterator.hasNext(); )
            {
                final EProjectCycle eProjectCycle = iterator.next();
                ProjectRelationshipFilter filter = getRootFilter();

                boolean include = true;
                for ( final ProjectRelationship<?> rel : eProjectCycle )
                {
                    if ( !filter.accept( rel ) )
                    {
                        include = false;
                        break;
                    }

                    filter = filter.getChildFilter( rel );
                }

                if ( !include )
                {
                    iterator.remove();
                }
            }

        }

        this.cycles = cycles;
    }

    @Override
    protected boolean shouldTraverseEdge( final ProjectRelationship<?> relationship, final List<ProjectRelationship<?>> path, final int pass )
    {
        final ProjectVersionRef decl = relationship.getDeclaring();

        ProjectVersionRef target = relationship.getTarget();
        if ( target instanceof ArtifactRef )
        {
            target = ( (ArtifactRef) target ).asProjectVersionRef();
        }

        final ProjectRef baseDecl = new ProjectRef( decl.getGroupId(), decl.getArtifactId() );
        final ProjectRef baseTgt = new ProjectRef( target.getGroupId(), target.getArtifactId() );

        int declIdx = order.indexOf( baseDecl );
        final int tgtIdx = order.indexOf( baseTgt );
        if ( declIdx < 0 )
        {
            declIdx = order.size();
            order.add( baseDecl );
        }

        if ( tgtIdx < 0 )
        {
            order.add( declIdx, baseTgt );
        }

        return true;
    }

    @Override
    public void endTraverse( final int pass )
        throws GraphDriverException
    {
        super.endTraverse( pass );
    }

}
