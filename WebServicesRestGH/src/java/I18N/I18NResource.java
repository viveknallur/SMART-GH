/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package I18N;

import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.Produces;
import javax.ws.rs.GET;
import javax.ws.rs.Path;

import com.graphhopper.util.Helper;
import com.graphhopper.util.TranslationMap;
import com.graphhopper.util.Translation;
import java.util.Locale;
import javax.inject.Inject;
import org.json.JSONException;
import org.json.JSONObject;



/**
 * REST Web Service
 *
 * @author amola
 */
@Path("i18n")
public class I18NResource {

    @Context
    private UriInfo context;
    
    //@Inject
    private TranslationMap map;

    /**
     * Creates a new instance of I18NResource
     */
    public I18NResource() {
    }

    /**
     * Retrieves representation of an instance of I18N.I18NResource
     * @return an instance of java.lang.String
     */
    @GET
    @Produces("application/json")
    public String getJson() throws JSONException{
        //TODO return proper representation object
        //throw new UnsupportedOperationException();
       
            String locale = "";
            Translation tr = map.get(locale);
            JSONObject json = new JSONObject();
            if (tr != null && !Locale.US.equals(tr.getLocale()))
                json.put("default", tr.asMap());

            json.put("locale", locale.toString());
            json.put("en", map.get("en").asMap());
            
            return json.toString();
       
    }

    
    
}
