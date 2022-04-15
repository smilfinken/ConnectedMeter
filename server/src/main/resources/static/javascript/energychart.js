async function loadData() {
  await fetch('/meter/api/data')
    .then(response => response.json())
    .then(data => pushData(data))
}

function pushData(data) {
  var timestamps = []
  var usageData = []
  var productionData = []
  var balanceData = []
  data.forEach(item => {
    timestamps.push(new Date(item.timestamp).toTimeString().substr(0,8))
    usageData.push(item.usage)
    productionData.push(item.production)
    balanceData.push(item.balance)
  })
  new Chart(document.getElementById("energy-data-chart"), {
    type: 'line',
    data: {
      labels: timestamps,
      datasets: [
        {
          label: "Total energy usage",
          backgroundColor: "blue",
          data: usageData
        },
        {
          label: "Solar power production",
          backgroundColor: "orange",
          data: productionData
        },
        {
          label: "In/out balance",
          backgroundColor: "red",
          data: balanceData
        }
      ]
    },
    options: {
      title: { display: true, text: "Energy data" },
      scales: { x: { display: false } },
      legend: { display: true },
      responsive: true,
      maintainAspectRatio: false
    }
  });
}