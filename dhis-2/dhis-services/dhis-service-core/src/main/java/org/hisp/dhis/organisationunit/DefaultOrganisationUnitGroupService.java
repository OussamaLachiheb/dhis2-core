package org.hisp.dhis.organisationunit;

/*
 * Copyright (c) 2004-2018, University of Oslo
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * Neither the name of the HISP project nor the names of its contributors may
 * be used to endorse or promote products derived from this software without
 * specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

import org.hisp.dhis.commons.filter.Filter;
import org.hisp.dhis.commons.filter.FilterUtils;
import org.hisp.dhis.user.CurrentUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * @author Torgeir Lorange Ostby
 */
@Service( "org.hisp.dhis.organisationunit.OrganisationUnitGroupService" )
@Transactional
public class DefaultOrganisationUnitGroupService
    implements OrganisationUnitGroupService
{
    // -------------------------------------------------------------------------
    // Dependencies
    // -------------------------------------------------------------------------

    private final OrganisationUnitGroupStore organisationUnitGroupStore;

    private final OrganisationUnitGroupSetStore organisationUnitGroupSetStore;

    public DefaultOrganisationUnitGroupService(OrganisationUnitGroupStore organisationUnitGroupStore, OrganisationUnitGroupSetStore organisationUnitGroupSetStore) {

        checkNotNull( organisationUnitGroupSetStore );
        checkNotNull( organisationUnitGroupStore );

        this.organisationUnitGroupStore = organisationUnitGroupStore;
        this.organisationUnitGroupSetStore = organisationUnitGroupSetStore;
    }

    @Autowired
    private CurrentUserService currentUserService;

    @Autowired
    private OrganisationUnitService organisationUnitService;

    // -------------------------------------------------------------------------
    // OrganisationUnitGroup
    // -------------------------------------------------------------------------

    @Override
    public long addOrganisationUnitGroup( OrganisationUnitGroup organisationUnitGroup )
    {
        organisationUnitGroupStore.save( organisationUnitGroup );

        return organisationUnitGroup.getId();
    }

    @Override
    public void updateOrganisationUnitGroup( OrganisationUnitGroup organisationUnitGroup )
    {
        organisationUnitGroupStore.update( organisationUnitGroup );
    }

    @Override
    public void deleteOrganisationUnitGroup( OrganisationUnitGroup organisationUnitGroup )
    {
        organisationUnitGroupStore.delete( organisationUnitGroup );
    }

    @Override
    public OrganisationUnitGroup getOrganisationUnitGroup( long id )
    {
        return organisationUnitGroupStore.get( id );
    }

    @Override
    public OrganisationUnitGroup getOrganisationUnitGroup( String uid )
    {
        return organisationUnitGroupStore.getByUid( uid );
    }

    @Override
    public List<OrganisationUnitGroup> getAllOrganisationUnitGroups()
    {
        return organisationUnitGroupStore.getAll();
    }

    @Override
    public List<OrganisationUnitGroup> getOrganisationUnitGroupsWithGroupSets()
    {
        return organisationUnitGroupStore.getOrganisationUnitGroupsWithGroupSets();
    }

    // -------------------------------------------------------------------------
    // OrganisationUnitGroupSet
    // -------------------------------------------------------------------------

    @Override
    public long addOrganisationUnitGroupSet( OrganisationUnitGroupSet organisationUnitGroupSet )
    {
        organisationUnitGroupSetStore.save( organisationUnitGroupSet );

        return organisationUnitGroupSet.getId();
    }

    @Override
    public void updateOrganisationUnitGroupSet( OrganisationUnitGroupSet organisationUnitGroupSet )
    {
        organisationUnitGroupSetStore.update( organisationUnitGroupSet );
    }

    @Override
    public void deleteOrganisationUnitGroupSet( OrganisationUnitGroupSet organisationUnitGroupSet )
    {
        organisationUnitGroupSetStore.delete( organisationUnitGroupSet );
    }

    @Override
    public OrganisationUnitGroupSet getOrganisationUnitGroupSet( long id )
    {
        return organisationUnitGroupSetStore.get( id );
    }

    @Override
    public OrganisationUnitGroupSet getOrganisationUnitGroupSet( String uid )
    {
        return organisationUnitGroupSetStore.getByUid( uid );
    }

    @Override
    public List<OrganisationUnitGroupSet> getAllOrganisationUnitGroupSets()
    {
        return organisationUnitGroupSetStore.getAll();
    }

    @Override
    public List<OrganisationUnitGroupSet> getCompulsoryOrganisationUnitGroupSets()
    {
        List<OrganisationUnitGroupSet> groupSets = new ArrayList<>();

        for ( OrganisationUnitGroupSet groupSet : getAllOrganisationUnitGroupSets() )
        {
            if ( groupSet.isCompulsory() )
            {
                groupSets.add( groupSet );
            }
        }

        return groupSets;
    }

    @Override
    public List<OrganisationUnitGroupSet> getCompulsoryOrganisationUnitGroupSetsWithMembers()
    {
        return FilterUtils.filter( getAllOrganisationUnitGroupSets(), new Filter<OrganisationUnitGroupSet>()
        {
            @Override
            public boolean retain( OrganisationUnitGroupSet object )
            {
                return object.isCompulsory() && object.hasOrganisationUnitGroups();
            }
        } );
    }

    public OrganisationUnitGroup getOrganisationUnitGroup( OrganisationUnitGroupSet groupSet, OrganisationUnit unit )
    {
        for ( OrganisationUnitGroup group : groupSet.getOrganisationUnitGroups() )
        {
            if ( group.getMembers().contains( unit ) )
            {
                return group;
            }
        }

        return null;
    }

    @Override
    public List<OrganisationUnitGroupSet> getCompulsoryOrganisationUnitGroupSetsNotAssignedTo(
        OrganisationUnit organisationUnit )
    {
        List<OrganisationUnitGroupSet> groupSets = new ArrayList<>();

        for ( OrganisationUnitGroupSet groupSet : getCompulsoryOrganisationUnitGroupSets() )
        {
            if ( !groupSet.isMemberOfOrganisationUnitGroups( organisationUnit ) && groupSet.hasOrganisationUnitGroups() )
            {
                groupSets.add( groupSet );
            }
        }

        return groupSets;
    }

    @Override
    public void mergeWithCurrentUserOrganisationUnits( OrganisationUnitGroup organisationUnitGroup, Collection<OrganisationUnit> mergeOrganisationUnits )
    {
        Set<OrganisationUnit> organisationUnits = new HashSet<>( organisationUnitGroup.getMembers() );

        Set<OrganisationUnit> userOrganisationUnits = new HashSet<>();

        for ( OrganisationUnit organisationUnit : currentUserService.getCurrentUser().getOrganisationUnits() )
        {
            userOrganisationUnits.addAll( organisationUnitService.getOrganisationUnitWithChildren( organisationUnit.getUid() ) );
        }

        organisationUnits.removeAll( userOrganisationUnits );
        organisationUnits.addAll( mergeOrganisationUnits );

        organisationUnitGroup.updateOrganisationUnits( organisationUnits );

        updateOrganisationUnitGroup( organisationUnitGroup );
    }
}
