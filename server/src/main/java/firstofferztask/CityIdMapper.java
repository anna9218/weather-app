package firstofferztask;

public class CityIdMapper {
  private Integer id;
  private String name;
  private String country;

  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public CityIdMapper(Integer id, String name, String country) {
    this.id = id;
    this.name = name;
    this.country = country;
  }

  @Override
  public String toString() {
    return "CityIdMapper{" +
      "id='" + id + '\'' +
      ", city='" + name + '\'' +
      ", country='" + country + '\'' +
      '}';
  }

  public String getCity() {
    return name;
  }

  public void setCity(String city) {
    this.name = city;
  }

  public String getCountry() {
    return country;
  }

  public void setCountry(String country) {
    this.country = country;
  }
}
