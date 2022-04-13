async function loadData() {
  await fetch('/meter/api/data')
    .then(response => response.json())
    .then(data => pushData(data))
}

function pushData(data) {
  var timestamps = []
  var productionData = []
  var consumptionData = []
  data.forEach(item => {
    timestamps.push(new Date(item.timestamp).toISOString().substr(11,8))
    productionData.push(item.production)
    consumptionData.push(item.consumption)
  })
  new Chart(document.getElementById("energy-data-chart"), {
    type: 'line',
    data: {
      labels: timestamps,
      datasets: [
        {
          label: "Production",
          backgroundColor: "green",
          data: productionData
        },
        {
          label: "Consumption",
          backgroundColor: "red",
          data: consumptionData
        }
      ]
    },
    options: {
      responsive: false,
      legend: { display: true },
      title: { display: true, text: "Energy data" }
    }
  });
}