package ru.dubrovinalexeyleonidovich.mobileagent;

import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.NotNull;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.structure.BaseModel;

@Table(database = MyDatabase.class)
public class GPSCoordinates
        extends BaseModel{

    @PrimaryKey(autoincrement = true)
    @Column
    private long id;

    @Column
    private String userId;

    @Column
    private double longitude;

    @Column
    private double latitude;

    @Column
    private double accuracy;

    @NotNull
    @Column
    private long dateStamp;


    public long getId()
    {
        return id;
    }

    public void setId(long id)
    {
        this.id = id;
    }



    public String getUserId()
    {
        return userId;
    }

    public void setUserId(String userId)
    {
        this.userId = userId;
    }



    public double getLongitude()
    {
        return longitude;
    }

    public void setLongitude(double longitude)
    {
        this.longitude = longitude;
    }



    public double getLatitude()
    {
        return latitude;
    }

    public void setLatitude(double latitude)
    {
        this.latitude = latitude;
    }



    public long getDateStamp()
    {
        return dateStamp;
    }

    public void setDateStamp(long dateStamp)
    {
        this.dateStamp = dateStamp;
    }


    public double getAccuracy() {
        return accuracy;
    }

    public void setAccuracy(double accuracy) {
        this.accuracy = accuracy;
    }
}
