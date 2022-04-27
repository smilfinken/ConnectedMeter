async function loadChartData() {
  await fetch('/meter/api/hourlyData')
    .then(response => response.json())
    .then(data => processData(data))
}

function processData(data) {
  var timestamps = []
  var usageData = []
  var productionData = []
  var balanceData = []
  var balanceDataColours = []
  var indoorTemperatureData = []
  var outdoorTemperatureData = []

  function createTimeString(date) {
    startTime = date.toTimeString().substr(0,5)
    date.setMinutes(date.getMinutes() + 59)
    endTime = date.toTimeString().substr(0,5)
    return startTime + "-" + endTime
  }
  data.forEach(item => {
    timestamps.push(createTimeString(new Date(item.timestamp)))
    usageData.push(item.usage)
    productionData.push(item.production)
    balanceData.push(item.balance)
    balanceDataColours.push(item.balance < 0 ? "orangered" : "green")
    indoorTemperatureData.push(item.indoorTemperature)
    outdoorTemperatureData.push(item.outdoorTemperature)
  })
  var chart = new Chart(document.getElementById("energy-data-chart"), {
    type: 'bar',
    data: {
      labels: timestamps,
      datasets: [
        {
          label: "Solar power production",
          backgroundColor: "orange",
          type: "bar",
          data: productionData,
          yAxisId: "y",
          order: 11
        },
        {
          label: "Total energy usage",
          backgroundColor: "blue",
          type: "bar",
          data: usageData,
          yAxisId: "y",
          order: 12
        },
        {
          label: "Energy usage balance",
          backgroundColor: balanceDataColours,
          type: "bar",
          data: balanceData,
          yAxisId: "y",
          order: 13
        },
        {
          label: "Indoor temperature",
          backgroundColor: "gray",
          type: "line",
          data: indoorTemperatureData,
          yAxisID: "y1",
          order: 1
        },
        {
          label: "Outdoor temperature",
          backgroundColor: "black",
          type: "line",
          data: outdoorTemperatureData,
          yAxisID: "y1",
          order: 2
        }
      ]
    },
    options: {
      scales: {
        x: {
          text: "Hour starting at"
        },
        y: {
          position: "left"
        },
        y1: {
          position: "right"
        }
      },
      legend: {
        display: true,
        reverse: true
      },
      responsive: true,
      maintainAspectRatio: false,
      interaction: {
        mode: "index",
        intersect: false
      },
      stacked: false,
      plugins: {
        title: {
          display: true,
          text: "Energy data for the last 24 hours"
        }
      }
    }
  });
}