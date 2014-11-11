/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Route;

import java.util.Set;
import javax.ws.rs.core.Application;

/**
 *
 * @author elgammaa
 */
@javax.ws.rs.ApplicationPath("webresources")
public class ApplicationConfig extends Application {

    @Override
    public Set<Class<?>> getClasses() {
        Set<Class<?>> resources = new java.util.HashSet<>();
        addRestResourceClasses(resources);
        return resources;
    }

    /**
     * Do not modify addRestResourceClasses() method.
     * It is automatically populated with
     * all resources defined in the project.
     * If required, comment out calling this method in getClasses().
     */
    private void addRestResourceClasses(Set<Class<?>> resources) {
        resources.add(I18N.EnvResource.class);
        resources.add(I18N.I18NResource.class);
        resources.add(Info.InfoResource.class);
        resources.add(ReadGHConfig.readConfigResource.class);
        resources.add(Route.RouteResource.class);
        resources.add(Route.RoutegpxResource.class);
    }
    
}
