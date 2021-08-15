import axios from "axios";

const currentForecastsApi = "http://localhost:8080/currentforecasts";
const forecastsApi = "http://localhost:8080/forecasts";

const errors = document.querySelector(".errors");
const loading = document.querySelector(".loading");

const errorsForecast = document.querySelector(".errors-forecast");
const loadingForecast = document.querySelector(".loading-forecast");

// vars for current weather data
const currentCity = document.querySelector(".current-city");
const currentCountry = document.querySelector(".current-country");
const currentTemp = document.querySelector(".current-temp");
const currentHumidity = document.querySelector(".current-humidity");
const currentDate = document.querySelector(".current-date");
const weatherResults = document.querySelector(".weather-result-container");


// vars for forecast
const forecastResults = document.querySelector(".forecast-result-container");

const day1 = document.querySelector(".day-1");
const dateDay1 = document.querySelector(".date-day-1");
const tempDay1 = document.querySelector(".temp-day-1");
const minTempDay1 = document.querySelector(".min-temp-day-1");
const maxTempDay1 = document.querySelector(".max-temp-day-1");

const day2 = document.querySelector(".day-2");
const dateDay2 = document.querySelector(".date-day-2");
const tempDay2 = document.querySelector(".temp-day-2");
const minTempDay2 = document.querySelector(".min-temp-day-2");
const maxTempDay2 = document.querySelector(".max-temp-day-2");

const day3 = document.querySelector(".day-3");
const dateDay3 = document.querySelector(".date-day-3");
const tempDay3 = document.querySelector(".temp-day-3");
const minTempDay3 = document.querySelector(".min-temp-day-3");
const maxTempDay3 = document.querySelector(".max-temp-day-3");

const day4 = document.querySelector(".day-4");
const dateDay4 = document.querySelector(".date-day-4");
const tempDay4 = document.querySelector(".temp-day-4");
const minTempDay4 = document.querySelector(".min-temp-day-4");
const maxTempDay4 = document.querySelector(".max-temp-day-4");

const day5 = document.querySelector(".day-5");
const dateDay5 = document.querySelector(".date-day-5");
const tempDay5 = document.querySelector(".temp-day-5");
const minTempDay5 = document.querySelector(".min-temp-day-5");
const maxTempDay5 = document.querySelector(".max-temp-day-5");


// welcome msg vars
const welcome = document.querySelector(".welcome");
const welcomeBack = document.querySelector(".welcome-back");

// reset button
const resetBtn = document.querySelector(".location-btn");

// method to search by city name and country code
const fetchCurrentWeatherData = async (cityName, countryCode) => {
  loading.style.display = "block";
  errors.textContent = "";
  try {
    const response = await axios.get(`${currentForecastsApi}?city=${cityName}&country=${countryCode}`);
    console.log(response);
    loading.style.display = "none";

    console.log(response.data);
    currentCity.textContent = response.data.city.charAt(0).toUpperCase() + response.data.city.slice(1);
    currentCountry.textContent = response.data.country.toUpperCase();
    currentTemp.textContent = response.data.temp;
    currentHumidity.textContent = response.data.humidity;
    currentDate.textContent = response.data.date;

    weatherResults.style.display = "block";
    form.style.display = "none";
    resetBtn.style.display = "block";
    forecastForm.style.display = "block";

  }
  catch (error) {
    loading.style.display = "none";
    weatherResults.style.display = "none";
    errors.textContent = "Sorry, but we have no data for the location you have requested.";
  }

};


const fetchForecast = async (days) => {
  loadingForecast.style.display = "block";
  errorsForecast.textContent = "";
  forecastResults.style.display = "none";

  try {
    const response = await axios.get(`${forecastsApi}?city=${localStorage.getItem("city")}&country=${localStorage.getItem("country")}&days=${days}`);
    loading.style.display = "none";

    console.log(localStorage.getItem("city").charAt(0).toUpperCase() + localStorage.getItem("city").slice(1));
    console.log(response.data);

    currentCity.textContent = localStorage.getItem("city").charAt(0).toUpperCase() + localStorage.getItem("city").slice(1);
    currentCountry.textContent = localStorage.getItem("country").toUpperCase();
    inputDays.textContent = days;

    day1.style.display = "none";
    day2.style.display = "none";
    day3.style.display = "none";
    day4.style.display = "none";
    day5.style.display = "none";

    if (days>0) {
      dateDay1.textContent = response.data.forecasts[0].date;
      tempDay1.textContent = response.data.forecasts[0].dayTemp;
      minTempDay1.textContent = response.data.forecasts[0].minTemp;
      maxTempDay1.textContent = response.data.forecasts[0].maxTemp;
      day1.style.display = "block";
    }
    if (days>1) {
      dateDay2.textContent = response.data.forecasts[1].date;
      tempDay2.textContent = response.data.forecasts[1].dayTemp;
      minTempDay2.textContent = response.data.forecasts[1].minTemp;
      maxTempDay2.textContent = response.data.forecasts[1].maxTemp;
      day2.style.display = "block";
    }
    if (days>2) {
      dateDay3.textContent = response.data.forecasts[2].date;
      tempDay3.textContent = response.data.forecasts[2].dayTemp;
      minTempDay3.textContent = response.data.forecasts[2].minTemp;
      maxTempDay3.textContent = response.data.forecasts[2].maxTemp;
      day3.style.display = "block";
    }
    if (days>3) {
      dateDay4.textContent = response.data.forecasts[3].date;
      tempDay4.textContent = response.data.forecasts[3].dayTemp;
      minTempDay4.textContent = response.data.forecasts[3].minTemp;
      maxTempDay4.textContent = response.data.forecasts[3].maxTemp;
      day4.style.display = "block";
    }
    if (days>4) {
      dateDay5.textContent = response.data.forecasts[4].date;
      tempDay5.textContent = response.data.forecasts[4].dayTemp;
      minTempDay5.textContent = response.data.forecasts[4].minTemp;
      maxTempDay5.textContent = response.data.forecasts[4].maxTemp;
      day5.style.display = "block";
    }

    
    forecastResults.style.display = "block";
    loadingForecast.style.display = "none";
    resetBtn.style.display = "block";
    forecastForm.style.display = "block";


  }
  catch (error) {
    loadingForecast.style.display = "none";
    weatherResults.style.display = "none";
    errors.textContent = "Sorry, but we have no data for the location you have requested.";
  }

};



// decide what should be displayed depending on the local storage
welcome.style.display = localStorage.getItem("city") === null ? "block" : "none";
welcomeBack.style.display = localStorage.getItem("city") === null ? "none" : "block";

weatherResults.style.display = localStorage.getItem("city") === null ? "none" : 
fetchCurrentWeatherData(localStorage.getItem("city"), localStorage.getItem("country"));

resetBtn.style.display = localStorage.getItem("city") === null ? "none" : "block";

forecastResults.style.display = "none";
loading.style.display = "none";
loadingForecast.style.display = "none";
errors.textContent = "";
errorsForecast.textContent = "";

// get the forms
const form = document.querySelector(".form-data");
form.style.display = localStorage.getItem("city") === null ? "block" : "none";
const forecastForm = document.querySelector(".form-data-forecast");
forecastForm.style.display = localStorage.getItem("city") === null ? "none" : "block";
// const resetLocationForm = document.querySelector(".form-reset");

// get the city name
const city = document.querySelector(".city-name");
// get the country name
const country = document.querySelector(".country-code");
// get the days for forecast
const inputDays = document.querySelector(".days");



// function to handle form submission
const handleWeatherSubmit = async event => {
  event.preventDefault();
  // store user's location
  if (typeof(Storage) !== "undefined") {
    localStorage.setItem("city", city.value);
    localStorage.setItem("country", country.value);
  }
  fetchCurrentWeatherData(city.value, country.value);
};

const handleForecast = async event => {
  event.preventDefault();
  console.log(inputDays.value);
  if (inputDays.value > 5) {
    errorsForecast.textContent = "Sorry, but 5 days is the limit for the forecast";
    forecastResults.style.display = "none";
  }
  else {
    errorsForecast.textContent = "";
    fetchForecast(inputDays.value);
  }
};

const handleReset = () => {
  // delete local storage
  localStorage.removeItem("city");
  localStorage.removeItem("country");

  weatherResults.style.display = "none";
  form.style.display = "block";
  welcomeBack.style.display = "none";
  welcome.style.display = "block";
  forecastForm.style.display = "none";
  forecastResults.style.display = "none";
  resetBtn.style.display = "none";
  errorsForecast.style.display = "none";
}

form.addEventListener("submit", event => handleWeatherSubmit(event));
forecastForm.addEventListener("submit", event => handleForecast(event));
resetBtn.addEventListener("click", () => handleReset());

