/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package servicios;

import com.google.gson.Gson;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PUT;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * REST Web Service
 *
 * @author iFranco
 */
@Path("check")
public class servicio {

    @Context
    private UriInfo context;

    /**
     * Creates a new instance of GenericResource
     */
    public servicio() {
    }

    /**
     * Retrieves representation of an instance of Paquete.ServiciosGomoku
     *
     * @return an instance of java.lang.String
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getJson(String access) {
        //TODO return proper representation object
        JsonObjectBuilder jOB = Json.createObjectBuilder();
        jOB.add("reply", "get method ok " + access);
        JsonObject jO = jOB.build();
        return Response.ok(jO.toString()).build();
        //header('Access-Control-Allow-Origin', '*').
        //header('Access-Control-Allow-Methods', 'POST, GET, PUT, UPDATE, OPTIONS').
        //header('Access-Control-Allow-Headers', 'Content-Type, Accept, X-Requested-With').build();        
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response getPost(String content) throws IOException {
        System.out.println("-------------------------------------------------");
        Gson gson = new Gson();
        Objeto rec = gson.fromJson(content, Objeto.class);
        String urlPeticion = rec.getUrl();
        /* CAPTURO URL DE PETICION */
        System.out.println("Post: " + urlPeticion);
        String bodyJSX = "";

        File file = new File("C:\\Users\\iFiL\\Documents\\WebProjects\\"
                + "inf-sec-ha\\src\\components\\Body.jsx");
        FileInputStream fis = new FileInputStream(file);
        byte[] data = new byte[(int) file.length()];
        fis.read(data);
        fis.close();
        bodyJSX = new String(data, "UTF-8");
        /* CARGO TODO BODYJSX A UN STRING */

        Pattern patternRoot = Pattern.compile("root: '(.*?)',");
        Matcher matcherRoot = patternRoot.matcher(bodyJSX);
        String urlRoot = matcherRoot.find() ? matcherRoot.group(1) : "http://noRoot.ngrok.io";
        /* CAPTURO URL ROOT DE BODYJSX */
        System.out.println("urlRoot: " + urlRoot);

        Pattern patternMirror = Pattern.compile("mirror: '(.*?)',");
        Matcher matcherMirror = patternMirror.matcher(bodyJSX);
        String urlMirror = matcherMirror.find() ? matcherMirror.group(1) : "http://noMirror.ngrok.io";
        /* CAPTURO URL MIRROR DE BODYJSX */
        System.out.println("urlMirror: " + urlMirror);

        URL urlPost = new URL(urlRoot);
        HttpURLConnection conPost = (HttpURLConnection) urlPost.openConnection();
        conPost.setRequestMethod("POST");
        conPost.setRequestProperty("Content-Type", "application/json; utf-8");
        conPost.setRequestProperty("Accept", "application/json");
        conPost.setDoOutput(true);
        String jsonPost
                = "{\"crud\": \"read\", \"index\": \"0\", \"codigo\": \"empty\"}";
        /* PREPARO PARAMETROS PARA POST */
        try (OutputStream os = conPost.getOutputStream()) {
            byte[] input = jsonPost.getBytes("utf-8");
            os.write(input, 0, input.length);
        } catch (Exception e) {
        }

        String respuesta = "";/* AQUI CAPTURARÉ LA RESPUESTA */

        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(conPost.getInputStream(), "utf-8"))) {
            StringBuilder response = new StringBuilder();
            String responseLine = null;
            while ((responseLine = br.readLine()) != null) {
                response.append(responseLine.trim());
            }
            respuesta = response.toString();
        } catch (Exception e) {
        }

        /* SI RESPUESTA INDICA APAGADO Y VIENE UN SERVICIO OK, LO PONGO COMO ROOT */
        if (respuesta.equals("null") || respuesta.equals("")) {            
            bodyJSX = bodyJSX.replace(urlMirror, "http://mirror.ngrok.io/inf-sec-php-ser/servicios-php.php");
            bodyJSX = bodyJSX.replace(urlRoot, urlPeticion);            
            BufferedWriter writer = new BufferedWriter(new FileWriter(file));
            writer.write(bodyJSX);
            writer.close();
            System.out.println("Cambió root");
        } else {
            /* SI SÍ FUNCIONA EL ROOT Y VIENE UN SERVICIO OK, TENEMOS QUE 
            SETEARLO COMO MIRROR Y ENVIARLE EL ROOT, Y DE SER EL ROOT, ENVIARLE 
            EL MIRROR */
            if (urlPeticion.equals(urlRoot)) {
                System.out.println("Retornando Mirror: " + urlMirror);
                return Response.ok(gson.toJson("{\"url\": \"" + urlMirror + "\"}")).build();
            } else if (urlPeticion.equals(urlMirror)) {
                System.out.println("Retornando Root: " + urlRoot);
                return Response.ok(gson.toJson("{\"url\": \"" + urlRoot + "\"}")).build();
            } else {
                bodyJSX = bodyJSX.replace(urlMirror, urlPeticion);
                BufferedWriter writer = new BufferedWriter(new FileWriter(file));
                writer.write(bodyJSX);
                writer.close();
                System.out.println("Cambió mirror");
            }
        }

        return Response.ok(gson.toJson("{\"url\": \"" + urlRoot + "\"}")).build();
    }

    /**
     * PUT method for updating or creating an instance of ServiciosGomoku
     *
     * @param content representation for the resource
     */
    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    public void putJson(String content) {
    }

    public class Objeto {

        private String url;

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

    }
}