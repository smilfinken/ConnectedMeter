async function loadDailyStats() {
  await fetch('/meter/api/dailyProduction/2')
    .then(response => response.json())
    .then(data => processProductionData(data))
  for (var index = 0; index <= 2; index++) {
    await fetch('/meter/api/dailyBalance/' + index)
      .then(response => response.json())
      .then(data => processBalanceData(index, data))
  }
}

function processProductionData(data) {
  for (var day = 0; day <=2; day++) {
    document.getElementById('production-' + day + '-daysago').appendChild(document.createTextNode(Math.floor(data[day].production) + " Wh"))
  }
}

function processBalanceData(index, data) {
  var element = document.getElementById('balance-' + index + '-daysago')
  var value = Math.floor(data[1].value - data[0].value)
  element.appendChild(document.createTextNode(value + " Wh"))
  element.style.color = value < 0 ? 'orangered' : 'green'
}