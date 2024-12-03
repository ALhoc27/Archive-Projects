package ru.dubrovinalexeyleonidovich.mobileagent;

import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Url;

public interface RequestsApi
{
    String unloadGPSCoordinatesURL = "{server}/{baseName}/hs/MobileAgentService/Coordinates";

    @POST("/{baseName}/hs/MobileAgentService/Coordinates")
    Call<JsonObject> unloadGPSCoordinates(
//            @Url String url,
//            @Path("server") String server,
            @Path("baseName") String baseName,
            @Body List<GPSCoordinates> gpsCoordinates
            );

}
