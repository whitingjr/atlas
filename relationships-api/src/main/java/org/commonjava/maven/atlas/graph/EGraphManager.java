package org.commonjava.maven.atlas.graph;

import static org.commonjava.maven.atlas.graph.model.GraphView.GLOBAL;

import java.io.Closeable;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

import org.commonjava.maven.atlas.graph.filter.ProjectRelationshipFilter;
import org.commonjava.maven.atlas.graph.model.EProjectDirectRelationships;
import org.commonjava.maven.atlas.graph.model.EProjectGraph;
import org.commonjava.maven.atlas.graph.model.EProjectKey;
import org.commonjava.maven.atlas.graph.model.EProjectWeb;
import org.commonjava.maven.atlas.graph.model.GraphView;
import org.commonjava.maven.atlas.graph.rel.ProjectRelationship;
import org.commonjava.maven.atlas.graph.rel.RelationshipType;
import org.commonjava.maven.atlas.graph.spi.EGraphDriver;
import org.commonjava.maven.atlas.graph.spi.GraphDriverException;
import org.commonjava.maven.atlas.graph.workspace.GraphWorkspace;
import org.commonjava.maven.atlas.graph.workspace.GraphWorkspaceConfiguration;
import org.commonjava.maven.atlas.graph.workspace.GraphWorkspaceListener;
import org.commonjava.maven.atlas.ident.ref.ProjectRef;
import org.commonjava.maven.atlas.ident.ref.ProjectVersionRef;
import org.commonjava.maven.atlas.ident.version.SingleVersion;
import org.commonjava.util.logging.Logger;

public class EGraphManager
    implements Closeable, GraphWorkspaceListener
{

    private static final String TEMP_WS = "is-temporary";

    private final Logger logger = new Logger( getClass() );

    private final EGraphDriver rootDriver;

    public EGraphManager( final EGraphDriver rootDriver )
    {
        this.rootDriver = rootDriver;
    }

    public Set<ProjectRelationship<?>> storeRelationships( final ProjectRelationship<?>... rels )
    {
        return rootDriver.addRelationships( rels );
    }

    public Set<ProjectRelationship<?>> storeRelationships( final Collection<ProjectRelationship<?>> rels )
    {
        return rootDriver.addRelationships( rels.toArray( new ProjectRelationship<?>[rels.size()] ) );
    }

    public EProjectGraph createGraph( final GraphWorkspace workspace, final EProjectDirectRelationships rels )
    {
        final ProjectVersionRef project = rels.getKey()
                                              .getProject();

        rootDriver.addRelationships( rels.getExactAllRelationships()
                                         .toArray( new ProjectRelationship[] {} ) );

        return getGraph( workspace, null, project );
    }

    public EProjectGraph getGraph( final GraphWorkspace workspace, final ProjectVersionRef project )
    {
        return getGraph( workspace, null, project );
    }

    public EProjectGraph getGraph( final GraphWorkspace workspace, final ProjectRelationshipFilter filter, final ProjectVersionRef project )
    {
        if ( !rootDriver.containsProject( GLOBAL, project ) || rootDriver.isMissing( GLOBAL, project ) )
        {
            return null;
        }

        return new EProjectGraph( workspace, rootDriver, filter, project );
    }

    public EProjectWeb getWeb( final GraphWorkspace workspace, final Collection<ProjectVersionRef> refs )
    {
        return getWeb( workspace, null, refs == null ? new ProjectVersionRef[0] : refs.toArray( new ProjectVersionRef[refs.size()] ) );
    }

    public EProjectWeb getWeb( final GraphWorkspace workspace, final ProjectRelationshipFilter filter, final Collection<ProjectVersionRef> refs )
    {
        return getWeb( workspace, null, refs == null ? new ProjectVersionRef[0] : refs.toArray( new ProjectVersionRef[refs.size()] ) );
    }

    public EProjectWeb getWeb( final GraphWorkspace workspace, final ProjectVersionRef... refs )
    {
        return getWeb( workspace, null, refs );
    }

    public EProjectWeb getWeb( final GraphWorkspace workspace, final ProjectRelationshipFilter filter, final ProjectVersionRef... refs )
    {
        for ( final ProjectVersionRef ref : refs )
        {
            if ( !rootDriver.containsProject( GLOBAL, ref ) || rootDriver.isMissing( GLOBAL, ref ) )
            {
                return null;
            }
        }

        return new EProjectWeb( workspace, rootDriver, filter, refs );
    }

    public GraphWorkspace createWorkspace( final GraphWorkspaceConfiguration config )
        throws GraphDriverException
    {
        return rootDriver.createWorkspace( config )
                         .addListener( this );
    }

    public boolean deleteWorkspace( final String id )
    {
        return rootDriver.deleteWorkspace( id );
    }

    public GraphWorkspace getWorkspace( final String id )
        throws GraphDriverException
    {
        final GraphWorkspace ws = rootDriver.loadWorkspace( id );
        if ( ws != null )
        {
            ws.addListener( this );
        }

        return ws;
    }

    public Set<GraphWorkspace> getAllWorkspaces()
    {
        return rootDriver.loadAllWorkspaces();
    }

    public boolean containsGraph( final ProjectVersionRef ref )
    {
        return containsGraph( GLOBAL, ref );
    }

    public boolean containsGraph( final GraphView view, final ProjectVersionRef ref )
    {
        return rootDriver.containsProject( view, ref ) && !rootDriver.isMissing( view, ref );
    }

    public boolean containsGraph( final GraphWorkspace workspace, final ProjectVersionRef ref )
    {
        return containsGraph( new GraphView( workspace ), ref );
    }

    public boolean containsGraph( final GraphWorkspace workspace, final ProjectRelationshipFilter filter, final ProjectVersionRef ref )
    {
        return containsGraph( new GraphView( workspace, filter ), ref );
    }

    public Set<ProjectRelationship<?>> findDirectRelationshipsFrom( final GraphView view, final ProjectVersionRef from,
                                                                    final boolean includeManagedInfo, final RelationshipType... types )
    {
        return rootDriver.getDirectRelationshipsFrom( view, from, includeManagedInfo, types );
    }

    public Set<ProjectRelationship<?>> findDirectRelationshipsTo( final GraphView view, final ProjectVersionRef to, final boolean includeManagedInfo,
                                                                  final RelationshipType... types )
    {
        return rootDriver.getDirectRelationshipsTo( view, to, includeManagedInfo, types );
    }

    public Set<ProjectRelationship<?>> findDirectRelationshipsFrom( final GraphWorkspace workspace, final ProjectVersionRef from,
                                                                    final boolean includeManagedInfo, final RelationshipType... types )
    {
        return findDirectRelationshipsFrom( new GraphView( workspace ), from, includeManagedInfo, types );
    }

    public Set<ProjectRelationship<?>> findDirectRelationshipsTo( final GraphWorkspace workspace, final ProjectVersionRef to,
                                                                  final boolean includeManagedInfo, final RelationshipType... types )
    {
        return findDirectRelationshipsTo( new GraphView( workspace ), to, includeManagedInfo, types );
    }

    public Set<ProjectVersionRef> getAllProjects( final GraphView view )
    {
        return rootDriver.getAllProjects( view );
    }

    public Set<ProjectVersionRef> getAllProjects( final GraphWorkspace workspace )
    {
        return getAllProjects( new GraphView( workspace ) );
    }

    public Set<ProjectVersionRef> getAllProjects()
    {
        return getAllProjects( GraphView.GLOBAL );
    }

    public Set<ProjectVersionRef> getAllIncompleteSubgraphs( final GraphView view )
    {
        return rootDriver.getMissingProjects( view );
    }

    public Set<ProjectVersionRef> getAllIncompleteSubgraphs( final GraphWorkspace workspace )
    {
        return getAllIncompleteSubgraphs( new GraphView( workspace ) );
    }

    public Set<ProjectVersionRef> getAllIncompleteSubgraphs()
    {
        return getAllIncompleteSubgraphs( GLOBAL );
    }

    public Set<ProjectVersionRef> getAllVariableSubgraphs( final GraphView view )
    {
        return rootDriver.getVariableProjects( view );
    }

    public Set<ProjectVersionRef> getAllVariableSubgraphs( final GraphWorkspace workspace )
    {
        return getAllVariableSubgraphs( new GraphView( workspace ) );
    }

    public Set<ProjectVersionRef> getAllVariableSubgraphs()
    {
        return getAllVariableSubgraphs( GLOBAL );
    }

    public Map<String, String> getMetadata( final ProjectVersionRef ref )
    {
        return rootDriver.getMetadata( ref );
    }

    public void addMetadata( final EProjectKey key, final String name, final String value )
    {
        rootDriver.addMetadata( key.getProject(), name, value );
    }

    public void setMetadata( final EProjectKey key, final Map<String, String> metadata )
    {
        rootDriver.setMetadata( key.getProject(), metadata );
    }

    public void addMetadata( final ProjectVersionRef project, final String name, final String value )
    {
        rootDriver.addMetadata( project, name, value );
    }

    public void setMetadata( final ProjectVersionRef project, final Map<String, String> metadata )
    {
        rootDriver.setMetadata( project, metadata );
    }

    public void reindex()
        throws GraphDriverException
    {
        rootDriver.reindex();
    }

    public Set<ProjectVersionRef> getProjectsWithMetadata( final String key )
    {
        return getProjectsWithMetadata( GLOBAL, key );
    }

    public Set<ProjectVersionRef> getProjectsWithMetadata( final GraphWorkspace workspace, final String key )
    {
        return getProjectsWithMetadata( new GraphView( workspace ), key );
    }

    public Set<ProjectVersionRef> getProjectsWithMetadata( final GraphView view, final String key )
    {
        return rootDriver.getProjectsWithMetadata( view, key );
    }

    public void addDisconnectedProject( final ProjectVersionRef ref )
    {
        rootDriver.addDisconnectedProject( ref );
    }

    @Override
    public void close()
        throws IOException
    {
        rootDriver.close();
    }

    @Override
    public void selectionAdded( final GraphWorkspace workspace, final ProjectVersionRef ref, final SingleVersion version )
        throws GraphDriverException
    {
        rootDriver.selectVersionFor( ref, version, workspace.getId() );
    }

    @Override
    public void wildcardSelectionAdded( final GraphWorkspace workspace, final ProjectRef ref, final SingleVersion version )
        throws GraphDriverException
    {
        rootDriver.selectVersionForAll( ref, version, workspace.getId() );
    }

    @Override
    public void closed( final GraphWorkspace workspace )
    {
        if ( workspace.getProperty( TEMP_WS, Boolean.class, Boolean.FALSE ) )
        {
            rootDriver.deleteWorkspace( workspace.getId() );
        }
        else
        {
            try
            {
                rootDriver.storeWorkspace( workspace );
            }
            catch ( final GraphDriverException e )
            {
                logger.error( "Failed to store updates for workspace: %s. Reason: %s", e, workspace, e.getMessage() );
            }
        }
    }

    @Override
    public void selectionsCleared( final GraphWorkspace workspace )
    {
        rootDriver.clearSelectedVersionsFor( workspace.getId() );
    }

    public Set<ProjectVersionRef> getProjectsMatching( final ProjectRef projectRef, final GraphWorkspace workspace )
    {
        return rootDriver.getProjectsMatching( projectRef, new GraphView( workspace ) );
    }

    @Override
    public void accessed( final GraphWorkspace ws )
    {
        // TODO: periodic loop to update last-access for eventual workspace-expiration logic...especially for temporary workspaces.
    }

    public GraphWorkspace createTemporaryWorkspace( final GraphWorkspaceConfiguration config )
        throws GraphDriverException
    {
        final GraphWorkspace ws = createWorkspace( config );
        ws.setProperty( TEMP_WS, Boolean.TRUE );

        return ws;
    }

    public void storeWorkspace( final GraphWorkspace ws )
        throws GraphDriverException
    {
        rootDriver.storeWorkspace( ws );
    }

}
