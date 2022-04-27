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
  var balance = Math.floor(data[1].sum - data[0].sum)
  element.appendChild(document.createTextNode(balance + " Wh"))
  element.style.color = balance < 0 ? 'orangered' : 'green'
}