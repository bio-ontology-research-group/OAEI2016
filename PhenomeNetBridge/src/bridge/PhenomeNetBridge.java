package bridge;

import eu.sealsproject.platform.res.domain.omt.IOntologyMatchingToolBridge;
import eu.sealsproject.platform.res.tool.api.ToolBridgeException;
import eu.sealsproject.platform.res.tool.api.ToolException;
import eu.sealsproject.platform.res.tool.api.ToolType;
import eu.sealsproject.platform.res.tool.impl.AbstractPlugin;
import marg.PhenomeNetMatcher;

import java.io.File;
import java.net.URL;

public class PhenomeNetBridge extends AbstractPlugin implements IOntologyMatchingToolBridge {

    /**
     * Aligns to ontologies specified via their URL and returns the
     * URL of the resulting alignment, which should be stored locally.
     *
     */
    public URL align(URL source, URL target) throws ToolBridgeException, ToolException {
        PhenomeNetMatcher phenomeNetMatcher;
        phenomeNetMatcher = new PhenomeNetMatcher();

        try {
            File alignmentFile = phenomeNetMatcher.match(source, target);
            return alignmentFile.toURI().toURL();
        }
        catch (Exception e) {
            throw new ToolBridgeException("cannot create file for resulting alignment", e);
        }

    }

    /**
     * This functionality is not supported by the tool. In case
     * it is invoced a ToolException is thrown.
     */
    public URL align(URL source, URL target, URL inputAlignment) throws ToolBridgeException, ToolException {
        throw new ToolException("functionality of called method is not supported");
    }

    /**
     * In our case the DemoMatcher can be executed on the fly. In case
     * prerequesites are required it can be checked here.
     */
    public boolean canExecute() {
        return true;
    }

    /**
     * The DemoMatcher is an ontology matching tool. SEALS supports the
     * evaluation of different tool types like e.g., reasoner and storage systems.
     */
    public ToolType getType() {
        return ToolType.OntologyMatchingTool;
    }

}