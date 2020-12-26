package com.example.firebaseapp.notifications;

import java.lang.reflect.Type;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface APIService {
    @Headers({
            "Content-Type: application/json",
            "Authorization: key=AAAAheTxPhU:APA91bEWc2dORm1BR8uRSB7tchANr52iDXa58ZpjI_EGw2f7A6KSabOHcdfalsC9jtNbP65aj21gqbYEphuqjTnlYuvM-1FbI2_J02zeo3bYcbZmMTchUkL0QTK6ZXz_BvGtWlQCAG2E"
    })

    @POST("fcm/send")
    Call<Response> sendNotification(@Body Sender body);
}
