/* =============================================================
   utils/charts.js
   Moteur de graphiques SVG natif — aucune librairie externe.
   Utilisé par Dashboard Admin ET Dashboard Vendeur.

   Exports :
     drawBarChart(containerId, monthlyData)     monthlyData: [{ label, montant }]
     drawDonutChart(containerId, categoryData)  categoryData: [{ categorie, ventes, pourcentage, couleur }]
   ============================================================= */

function drawBarChart(containerId, monthlyData) {
  const container = document.getElementById(containerId);
  if (!container) return;

  const hasData = monthlyData.some(d => d.montant > 0);
  if (!hasData) {
    container.innerHTML = `
      <div style="height:160px;display:flex;flex-direction:column;align-items:center;
                  justify-content:center;color:#7070A0;gap:8px">
        <div style="font-size:2rem;color:#4A30A0"><i class="fa-solid fa-chart-line"></i></div>
        <div style="font-size:0.85rem">Aucune donnée pour cette période</div>
        <div style="font-size:0.75rem">Les graphiques se rempliront avec vos premières commandes</div>
      </div>`;
    return;
  }

  const W = container.clientWidth || 540, H = 180;
  const PAD = { top: 20, right: 10, bottom: 30, left: 50 };
  const chartW = W - PAD.left - PAD.right, chartH = H - PAD.top - PAD.bottom;
  const maxVal = Math.max(...monthlyData.map(d => d.montant));
  const barW = Math.floor(chartW / monthlyData.length * 0.6);
  const gap  = Math.floor(chartW / monthlyData.length);

  let grid = "";
  for (let i = 0; i <= 4; i++) {
    const y = PAD.top + chartH - (i / 4) * chartH;
    const val = Math.round((i / 4) * maxVal);
    const label = val >= 1000 ? `${Math.round(val/1000)}k` : val;
    grid += `<line x1="${PAD.left}" y1="${y}" x2="${W-PAD.right}" y2="${y}" stroke="#DDD9F5" stroke-width="1" stroke-dasharray="${i===0?'0':'4,3'}"/>
      <text x="${PAD.left-6}" y="${y+4}" text-anchor="end" fill="#7070A0" font-size="10" font-family="Inter,sans-serif">${label}</text>`;
  }

  let bars = "";
  monthlyData.forEach((d, i) => {
    const barH = maxVal > 0 ? Math.round((d.montant / maxVal) * chartH) : 0;
    const x = PAD.left + i * gap + (gap - barW) / 2;
    const y = PAD.top + chartH - barH;
    const isMax = d.montant === maxVal && d.montant > 0;
    bars += `<rect x="${x}" y="${y}" width="${barW}" height="${barH}" fill="${isMax?'#F5A623':'#2D1B69'}" rx="4" ry="4">
      <title>${d.label} : ${d.montant.toLocaleString('fr-FR')} FCFA</title></rect>
      <text x="${x+barW/2}" y="${H-PAD.bottom+14}" text-anchor="middle" fill="#7070A0" font-size="10" font-family="Inter,sans-serif">${d.label}</text>`;
    if (barH > 18) {
      const lab = d.montant >= 1000 ? `${Math.round(d.montant/1000)}k` : d.montant;
      bars += `<text x="${x+barW/2}" y="${y-4}" text-anchor="middle" fill="${isMax?'#B07400':'#2D1B69'}" font-size="9" font-weight="700" font-family="Sora,sans-serif">${lab}</text>`;
    }
  });

  container.innerHTML = `<svg viewBox="0 0 ${W} ${H}" width="100%" height="${H}" xmlns="http://www.w3.org/2000/svg" style="overflow:visible">${grid}${bars}</svg>`;
}

function drawDonutChart(containerId, data) {
  const container = document.getElementById(containerId);
  if (!container) return;

  if (!data || !data.length) {
    container.innerHTML = `<div style="text-align:center;padding:2rem;color:#7070A0;font-size:0.85rem">Aucune donnée de vente disponible</div>`;
    return;
  }

  const total = data.reduce((s, d) => s + d.ventes, 0) || 1;
  const R = 40, CX = 50, CY = 50;
  let startAngle = -90, arcs = "";

  data.forEach(d => {
    const angle = (d.ventes / total) * 360;
    if (angle === 0) return;
    const endAngle = startAngle + angle;
    const large = angle > 180 ? 1 : 0;
    const toRad = a => (a * Math.PI) / 180;
    const x1 = CX + R * Math.cos(toRad(startAngle)), y1 = CY + R * Math.sin(toRad(startAngle));
    const x2 = CX + R * Math.cos(toRad(endAngle)),   y2 = CY + R * Math.sin(toRad(endAngle));
    arcs += `<path d="M${CX},${CY} L${x1.toFixed(1)},${y1.toFixed(1)} A${R},${R} 0 ${large},1 ${x2.toFixed(1)},${y2.toFixed(1)} Z" fill="${d.couleur}" opacity="0.9"><title>${d.categorie} : ${d.pourcentage}%</title></path>`;
    startAngle = endAngle;
  });

  const donutSVG = `<svg viewBox="0 0 100 100" width="80" height="80" style="flex-shrink:0">${arcs}
    <circle cx="${CX}" cy="${CY}" r="22" fill="white"/>
    <text x="${CX}" y="${CY+5}" text-anchor="middle" fill="#2D1B69" font-size="11" font-weight="700" font-family="Sora,sans-serif">${total}</text></svg>`;

  const list = data.map(d => `
    <div style="display:flex;align-items:center;gap:8px;margin-bottom:8px">
      <div style="width:10px;height:10px;border-radius:3px;background:${d.couleur};flex-shrink:0"></div>
      <div style="font-size:0.82rem;flex:1;color:#1A1A2E">${d.categorie}</div>
      <div style="flex:2;height:6px;background:#DDD9F5;border-radius:3px;overflow:hidden">
        <div style="height:100%;width:${d.pourcentage}%;background:${d.couleur};border-radius:3px;transition:width 0.8s ease"></div>
      </div>
      <div style="font-size:0.78rem;font-weight:700;color:#2D1B69;font-family:'Sora',sans-serif;min-width:30px;text-align:right">${d.pourcentage}%</div>
    </div>`).join("");

  container.innerHTML = `
    <div style="display:flex;align-items:center;gap:1rem;margin-bottom:1rem">
      ${donutSVG}
      <div style="font-size:0.78rem;color:#7070A0;line-height:1.5">
        <strong style="font-family:'Sora',sans-serif;color:#2D1B69;font-size:0.85rem">${total} ventes</strong><br>
        réparties sur ${data.length} catégorie(s)
      </div>
    </div>${list}`;
}
