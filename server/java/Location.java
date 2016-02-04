//Temporary location class for testing purposes
class Location {
    double longitude;
    double latitude;

    public Location(double latitude, double longitude){
        this.longitude = longitude;
        this.latitude = latitude;
    }

    public double getLongitude(){
        return longitude;
    }

    public double getLatitude(){
        return latitude;
    }
}