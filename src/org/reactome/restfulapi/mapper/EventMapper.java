/*
 * Created on Jun 5, 2012
 *
 */
package org.reactome.restfulapi.mapper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.gk.model.GKInstance;
import org.gk.model.PersistenceAdaptor;
import org.gk.model.ReactomeJavaConstants;
import org.reactome.restfulapi.ReactomeModelPostMapper;
import org.reactome.restfulapi.ReactomeToRESTfulAPIConverter;
import org.reactome.restfulapi.models.DatabaseObject;
import org.reactome.restfulapi.models.Event;
import org.reactome.restfulapi.models.Species;

/**
 * This class is used to do some post-processing for Event.
 * @author gwu
 *i
 */
@SuppressWarnings("unchecked")
public class EventMapper extends ReactomeModelPostMapper {
    
    public EventMapper() {
    }

    @Override
    public void fillDetailedView(GKInstance inst,
                                 DatabaseObject obj,
                                 ReactomeToRESTfulAPIConverter converter) throws Exception {
        // A sanity check
        if (!validParameters(inst, obj))
            return;
        Event event = (Event) obj;
        // Check for followingEvent
        fetchFollowingEvents(inst, event, converter);
        // Fetch Regulations referred by this inst.
        fetchRegulations(inst, event, converter);
        // Get species for orthologousEvents
        processOrthologousEvents(inst, event, converter);
    }
    
    private void processOrthologousEvents(GKInstance inst,
                                          Event event,
                                          ReactomeToRESTfulAPIConverter converter) throws Exception {
        List<Event> orthologusEvents = event.getOrthologousEvent();
        if (orthologusEvents == null || orthologusEvents.size() == 0)
            return;
        PersistenceAdaptor dba = inst.getDbAdaptor();
        // Fetch orthologous events
        for (Event oEvent : orthologusEvents) {
            Long dbId = oEvent.getDbId();
            GKInstance oInst = dba.fetchInstance(dbId);
            List<GKInstance> oSpecies = oInst.getAttributeValuesList(ReactomeJavaConstants.species);
            if (oSpecies == null || oSpecies.size() == 0)
                continue;
            List<Species> species = new ArrayList<Species>();
            for (GKInstance oSpecies1 : oSpecies) {
                Species tmp = (Species) converter.createObject(oSpecies1);
                species.add(tmp);
            }
            oEvent.setSpecies(species);
        }
    }

    @Override
    public void postProcess(GKInstance inst, 
                            DatabaseObject obj,
                            ReactomeToRESTfulAPIConverter converter) throws Exception {
    }

    /**
     * A helper method to fetch Regulation for the passed Event GKInstance.
     * @param inst
     * @param event
     * @param converter
     * @throws Exception
     */
    private void fetchRegulations(GKInstance inst,
                                  Event event,
                                  ReactomeToRESTfulAPIConverter converter) throws Exception {
        Collection<GKInstance> regulations = inst.getReferers(ReactomeJavaConstants.regulatedEntity);
        if (regulations == null || regulations.size() == 0)
            return;
        List<DatabaseObject> requirements = null;
        List<DatabaseObject> positiveRegulators = null;
        List<DatabaseObject> negativeRegulators = null;
        for (GKInstance regulation : regulations) {
            GKInstance regulator = (GKInstance) regulation.getAttributeValue(ReactomeJavaConstants.regulator);
            if (regulator == null)
                continue; // Just in case. This should not happen usually
            DatabaseObject converted = converter.createObject(regulator);
            // Have to check Requirement first since it is a subclass to PositiveRegulation
            if (regulation.getSchemClass().isa(ReactomeJavaConstants.Requirement)) {
                if (requirements == null)
                    requirements = new ArrayList<DatabaseObject>();
                requirements.add(converted);
            }
            else if (regulation.getSchemClass().isa(ReactomeJavaConstants.PositiveRegulation)) {    
                if (positiveRegulators == null)
                    positiveRegulators = new ArrayList<DatabaseObject>();
                positiveRegulators.add(converted);
            }
            else if (regulation.getSchemClass().isa(ReactomeJavaConstants.NegativeRegulation)) {
                if (negativeRegulators == null)
                    negativeRegulators = new ArrayList<DatabaseObject>();
                negativeRegulators.add(converted);
            }
        }
        event.setRequirements(requirements);
        event.setPositiveRegulators(positiveRegulators);
        event.setNegativeRegulators(negativeRegulators);
    }
    
    /**
     * A helper method to fetch following events from inst's referrers.
     * @param inst
     * @param currentEvent
     * @param converter
     * @throws Exception
     */
    private void fetchFollowingEvents(GKInstance inst, 
                                      Event currentEvent,
                                      ReactomeToRESTfulAPIConverter converter) throws Exception {
        Collection<GKInstance> followingEvent = inst.getReferers(ReactomeJavaConstants.precedingEvent);
        if (followingEvent != null && followingEvent.size() > 0) {
            List<Event> list = new ArrayList<Event>();
            for (GKInstance event : followingEvent) {
                Event converted = (Event) converter.createObject(event);
                list.add(converted);
            }
            currentEvent.setFollowingEvent(list);
        }
    }

    @Override
    protected boolean isValidObject(DatabaseObject obj) {
        return obj instanceof Event;
    }

    @Override
    public void postShellProcess(GKInstance inst, DatabaseObject obj) throws Exception {
        if (!validParameters(inst, obj))
            return;
        String releaseStatus = (String) inst.getAttributeValue(ReactomeJavaConstants.releaseStatus);
        if (releaseStatus != null) {
            Event event = (Event) obj;
            event.setReleaseStatus(releaseStatus);
        }
    }
    
}
