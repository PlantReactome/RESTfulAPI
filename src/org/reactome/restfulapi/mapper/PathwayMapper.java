/*
 * Created on Jun 5, 2012
 *
 */
package org.reactome.restfulapi.mapper;

import java.util.Collection;
import java.util.Iterator;

import org.gk.model.GKInstance;
import org.gk.model.ReactomeJavaConstants;
import org.gk.pathwaylayout.DiagramGeneratorFromDB;
import org.gk.persistence.MySQLAdaptor;
import org.reactome.restfulapi.ReactomeToRESTfulAPIConverter;
import org.reactome.restfulapi.models.DatabaseObject;
import org.reactome.restfulapi.models.Pathway;

/**
 * @author gwu
 *
 */
public class PathwayMapper extends EventMapper {
    
    public PathwayMapper() {
        
    }

    @Override
    public void postProcess(GKInstance inst, 
                            DatabaseObject obj,
                            ReactomeToRESTfulAPIConverter converter) throws Exception {
        super.postProcess(inst, obj, converter);
        if (!validParameters(inst, obj))
            return;
        addPathwayDiagramFlag(inst, obj);
    }

    private void addPathwayDiagramFlag(GKInstance inst, DatabaseObject obj) throws Exception {
        // Check if this Pathway has Diagram
        Pathway pathway = (Pathway) obj;
        pathway.setHasDiagram(false);
        if (inst.getDbAdaptor() instanceof MySQLAdaptor) {
            DiagramGeneratorFromDB diagramHelper = new DiagramGeneratorFromDB();
            diagramHelper.setMySQLAdaptor((MySQLAdaptor) inst.getDbAdaptor());
            GKInstance diagram = diagramHelper.getPathwayDiagram(inst);
            if (diagram != null)
                pathway.setHasDiagram(true);
        }
        else {
            Collection<?> diagrams = inst.getReferers(ReactomeJavaConstants.representedPathway);
            if (diagrams != null && diagrams.size() > 0) {
                for (Iterator<?> it = diagrams.iterator(); it.hasNext();) {
                    GKInstance diagram = (GKInstance) it.next();
                    if (diagram.getSchemClass().isa(ReactomeJavaConstants.PathwayDiagram)) {
                        pathway.setHasDiagram(true);
                        break;
                    }
                }
            }
        }
    }

    @Override
    public void postShellProcess(GKInstance inst, DatabaseObject obj)
            throws Exception {
        super.postShellProcess(inst, obj);
        addPathwayDiagramFlag(inst, obj);
    }
    
}
