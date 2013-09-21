package it.angelic.soulissclient.net.webserver;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.URLDecoder;

import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.entity.ContentProducer;
import org.apache.http.entity.EntityTemplate;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpRequestHandler;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

public class MessageCommandHandler implements HttpRequestHandler{
       
        private Context context = null;
       
        public MessageCommandHandler(Context context){
                this.context = context;
        }
       
        @Override
        public void handle(HttpRequest request, HttpResponse response, HttpContext httpContext) throws HttpException, IOException {
                String uriString = request.getRequestLine().getUri();
                Uri uri = Uri.parse(uriString);
                String message = URLDecoder.decode(uri.getQueryParameter("msg"));
               
                Log.i(it.angelic.soulissclient.Constants.TAG,"Message URI: " + uriString);
               
                //FAKE SEND FIXME
                Toast.makeText(context, message, Toast.LENGTH_SHORT);
               
                HttpEntity entity = new EntityTemplate(new ContentProducer() {
                public void writeTo(final OutputStream outstream) throws IOException {
                        OutputStreamWriter writer = new OutputStreamWriter(outstream, "UTF-8");
                        String resp = "SENT!!";
           
                        writer.write(resp);
                        writer.flush();
                }
        });
               
                response.setHeader("Content-Type", "text/html");
                response.setEntity(entity);
        }
       
      
}
