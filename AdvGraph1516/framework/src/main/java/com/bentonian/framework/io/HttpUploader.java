package com.bentonian.framework.io;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

public class HttpUploader {

  private String serverUrl;

  public HttpUploader(String serverUrl) throws MalformedURLException {
    this.serverUrl = serverUrl;
  }

  public void upload(File file, String remoteName) throws IOException {
    CloseableHttpClient httpclient = HttpClients.createDefault();

    HttpPost request = new HttpPost(serverUrl);
    request.setEntity(MultipartEntityBuilder.create()
        .addPart("name", new StringBody(remoteName, ContentType.TEXT_PLAIN))
        .addPart("fileToUpload", new FileBody(file))
        .build());

    try {
      CloseableHttpResponse response = httpclient.execute(request);
      try {
        System.out.println(readServerResponse(response.getEntity()));
      } finally {
        response.close();
      }
    } finally {
      httpclient.close();
    }
  }

  private String readServerResponse(HttpEntity resEntity) throws IOException {
    if (resEntity != null) {
      StringBuffer response = new StringBuffer();
      BufferedReader in = new BufferedReader(new InputStreamReader(resEntity.getContent()));
      String inputLine;
  
      while ((inputLine = in.readLine()) != null) {
        response.append(inputLine + "\n");
      }
      in.close();
      EntityUtils.consume(resEntity);
      return response.toString().trim();
    } else {
      return "<NO RESPONSE>";
    }
  }
}
