import { useState, useEffect } from 'react';
import {
  IoSparkles, IoWarningOutline, IoCheckmarkCircleOutline,
  IoBookmarkOutline, IoBookmark, IoStatsChartOutline, IoSearchOutline,
  IoRefreshOutline, IoChatbubbleEllipsesOutline, IoCloseOutline,
  IoChevronForwardOutline, IoShieldCheckmarkOutline, IoGitCompareOutline,
  IoHelpCircleOutline, IoSendOutline
} from 'react-icons/io5';
import { aiDiscoveryAPI, aiAssistantAPI, watchlistAPI, portfolioInsightsAPI } from '../../services/api';
import './AIDiscovery.css';

const INDUSTRIES = [
  { id: 'Information Technology', label: 'Information Technology', icon: '💻' },
  { id: 'Banking & Financial Services', label: 'Banking & Finance', icon: '🏦' },
  { id: 'Healthcare', label: 'Healthcare & Pharma', icon: '🏥' },
  { id: 'Automobiles', label: 'Automobiles & EV', icon: '🚗' },
  { id: 'Consumer Goods', label: 'Consumer Goods', icon: '🛍️' },
  { id: 'Energy', label: 'Energy & Renewables', icon: '⚡' },
  { id: 'Industrials', label: 'Industrials & Infra', icon: '🏗️' },
  { id: 'Telecommunications', label: 'Telecommunications', icon: '📡' },
];

const RISK_LEVELS = [
  { id: 'LOWER', label: 'Lower Risk', desc: 'Focus on balance sheet safety, low beta & dividends' },
  { id: 'MODERATE', label: 'Moderate Risk', desc: 'Balanced approach between capital growth and volatility' },
  { id: 'HIGHER', label: 'Higher Risk', desc: 'Growth-seeking, willing to accept higher price volatility' },
];

const HORIZONS = [
  { id: 'SHORT_TERM', label: 'Short Term (<1 yr)', desc: 'Near-term tactical ideas & liquidity focus' },
  { id: 'MEDIUM_TERM', label: 'Medium Term (1-3 yrs)', desc: 'Consistent business growth & fair valuation' },
  { id: 'LONG_TERM', label: 'Long Term (3+ yrs)', desc: 'Compounding quality, market leadership & moat' },
];

const GOALS = [
  { id: 'BALANCED_GROWTH', label: 'Balanced Growth' },
  { id: 'GROWTH', label: 'High Capital Growth' },
  { id: 'STABILITY', label: 'Capital Preservation' },
  { id: 'INCOME', label: 'Dividend Income' },
];

const AIDiscovery = () => {
  const [activeTab, setActiveTab] = useState('wizard'); // 'wizard', 'results', 'watchlist'
  const [selectedIndustries, setSelectedIndustries] = useState(['Information Technology']);
  const [riskPreference, setRiskPreference] = useState('MODERATE');
  const [investmentHorizon, setInvestmentHorizon] = useState('MEDIUM_TERM');
  const [investmentGoal, setInvestmentGoal] = useState('BALANCED_GROWTH');
  const [budgetRange, setBudgetRange] = useState('₹50,000 - ₹2,00,000');
  const [portfolioSource, setPortfolioSource] = useState('NEXORA_PORTFOLIO');

  // Loading state
  const [analyzing, setAnalyzing] = useState(false);
  const [analysisStep, setAnalysisStep] = useState(0);

  // Results state
  const [analysisData, setAnalysisData] = useState(null);
  const [filterScore, setFilterScore] = useState('ALL');
  const [selectedStockDetail, setSelectedStockDetail] = useState(null);

  // Compare state
  const [compareSymbols, setCompareSymbols] = useState([]);
  const [compareData, setCompareData] = useState(null);
  const [showCompareModal, setShowCompareModal] = useState(false);

  // Watchlist state
  const [watchlist, setWatchlist] = useState([]);
  const [watchlistLoading, setWatchlistLoading] = useState(false);

  // Chat Assistant Drawer
  const [chatOpen, setChatOpen] = useState(false);
  const [chatMessages, setChatMessages] = useState([
    {
      sender: 'ai',
      text: 'Hello! I am your explainable research assistant. Ask me questions about the scored candidates, why one ranked above another, or how adding a stock affects your sector exposure.',
      time: 'Just now'
    }
  ]);
  const [chatInput, setChatInput] = useState('');
  const [chatLoading, setChatLoading] = useState(false);

  // Notification Toast
  const [toastMessage, setToastMessage] = useState('');

  const showToast = (msg) => {
    setToastMessage(msg);
    setTimeout(() => setToastMessage(''), 3500);
  };

  useEffect(() => {
    loadWatchlist();
  }, []);

  const loadWatchlist = async () => {
    try {
      setWatchlistLoading(true);
      const res = await watchlistAPI.getAll();
      if (res.data?.success) {
        setWatchlist(res.data.data || []);
      }
    } catch (err) {
      console.warn('Watchlist fetch error', err);
    } finally {
      setWatchlistLoading(false);
    }
  };

  const handleToggleIndustry = (indId) => {
    if (selectedIndustries.includes(indId)) {
      if (selectedIndustries.length > 1) {
        setSelectedIndustries(selectedIndustries.filter(i => i !== indId));
      }
    } else {
      setSelectedIndustries([...selectedIndustries, indId]);
    }
  };

  const handleRunAnalysis = async () => {
    setAnalyzing(true);
    setAnalysisStep(1);

    const steps = [
      'Validating portfolio holdings and calculating sector exposures...',
      'Filtering market universe and verifying fundamental data...',
      'Computing transparent 100-point weighted scores...',
      'Grounding AI explanations and evaluating trade-offs...'
    ];

    let currentStep = 1;
    const interval = setInterval(() => {
      currentStep++;
      if (currentStep <= steps.length) {
        setAnalysisStep(currentStep);
      }
    }, 500);

    try {
      const payload = {
        industries: selectedIndustries,
        riskPreference,
        investmentHorizon,
        investmentGoal,
        budgetRange,
        portfolioSource
      };

      const res = await aiDiscoveryAPI.createAnalysis(payload);
      clearInterval(interval);

      if (res.data?.success) {
        setAnalysisData(res.data.data);
        setActiveTab('results');
        showToast('Analysis completed successfully!');
      }
    } catch (err) {
      clearInterval(interval);
      showToast(err.response?.data?.message || 'Failed to complete analysis');
    } finally {
      setAnalyzing(false);
      setAnalysisStep(0);
    }
  };

  const handleAddToWatchlist = async (instrumentId, symbol) => {
    try {
      const res = await watchlistAPI.add({ instrumentId, notes: 'Saved from AI Discovery analysis' });
      if (res.data?.success) {
        showToast(`Added ${symbol} to your Watchlist!`);
        loadWatchlist();
      }
    } catch (err) {
      showToast(err.response?.data?.message || 'Could not add to watchlist');
    }
  };

  const handleRemoveFromWatchlist = async (instrumentId, symbol) => {
    try {
      const res = await watchlistAPI.remove(instrumentId);
      if (res.data?.success) {
        showToast(`Removed ${symbol} from Watchlist`);
        loadWatchlist();
      }
    } catch (err) {
      showToast('Could not remove from watchlist');
    }
  };

  const handleToggleCompare = (symbol) => {
    if (compareSymbols.includes(symbol)) {
      setCompareSymbols(compareSymbols.filter(s => s !== symbol));
    } else {
      if (compareSymbols.length >= 3) {
        showToast('You can compare up to 3 stocks at a time');
        return;
      }
      setCompareSymbols([...compareSymbols, symbol]);
    }
  };

  const handleOpenCompare = async () => {
    if (compareSymbols.length < 2) {
      showToast('Select at least 2 stocks to compare');
      return;
    }
    try {
      const res = await aiDiscoveryAPI.compareCandidates({
        analysisId: analysisData?.analysisId,
        symbols: compareSymbols
      });
      if (res.data?.success) {
        setCompareData(res.data.data);
        setShowCompareModal(true);
      }
    } catch (err) {
      showToast('Failed to load comparison data');
    }
  };

  const handleSendChat = async (questionText) => {
    const textToSend = questionText || chatInput;
    if (!textToSend.trim()) return;

    const userMsg = { sender: 'user', text: textToSend, time: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }) };
    setChatMessages(prev => [...prev, userMsg]);
    if (!questionText) setChatInput('');
    setChatLoading(true);

    try {
      const res = await aiAssistantAPI.chat({
        analysisId: analysisData?.analysisId || 'latest',
        question: textToSend
      });
      if (res.data?.success) {
        const aiMsg = {
          sender: 'ai',
          text: res.data.data.answer,
          time: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }),
          citations: res.data.data.citations,
          suggested: res.data.data.suggestedQuestions
        };
        setChatMessages(prev => [...prev, aiMsg]);
      }
    } catch (err) {
      const errorMsg = { sender: 'ai', text: 'Sorry, I encountered an issue processing your research question. Please try again.', time: 'Just now' };
      setChatMessages(prev => [...prev, errorMsg]);
    } finally {
      setChatLoading(false);
    }
  };

  const filteredRecommendations = (analysisData?.recommendations || []).filter(rec => {
    if (filterScore === 'STRONG') return rec.score >= 80;
    if (filterScore === 'REASONABLE') return rec.score >= 65 && rec.score < 80;
    if (filterScore === 'LOWER') return rec.score < 65;
    return true;
  });

  return (
    <div className="discovery-container">
      {toastMessage && <div className="discovery-toast">{toastMessage}</div>}

      {/* Header Banner */}
      <div className="discovery-header">
        <div className="discovery-header-top">
          <div className="discovery-title-badge">
            <IoSparkles className="sparkle-icon" />
            <span>AI-POWERED STOCK RESEARCH</span>
          </div>
          <div className="discovery-nav-tabs">
            <button
              className={`nav-tab-btn ${activeTab === 'wizard' ? 'active' : ''}`}
              onClick={() => setActiveTab('wizard')}
            >
              Research Wizard
            </button>
            <button
              className={`nav-tab-btn ${activeTab === 'results' ? 'active' : ''}`}
              onClick={() => setActiveTab('results')}
              disabled={!analysisData}
            >
              Analysis Results {analysisData ? `(${analysisData.recommendations.length})` : ''}
            </button>
            <button
              className={`nav-tab-btn ${activeTab === 'watchlist' ? 'active' : ''}`}
              onClick={() => setActiveTab('watchlist')}
            >
              My Watchlist ({watchlist.length})
            </button>
          </div>
        </div>

        <div className="discovery-hero">
          <h1>Personalized Stock Discovery & Portfolio Insights</h1>
          <p>
            Combines verified market data, portfolio-aware concentration rules, and a 100-point transparent
            scoring system with grounded AI explanations.
          </p>
        </div>

        <div className="compliance-disclaimer-card">
          <IoShieldCheckmarkOutline className="disclaimer-icon" />
          <p>
            <strong>Informational Research Only:</strong> Recommendations are research suggestions based on disclosed criteria.
            Nexora does not promise returns or predict future prices, and no automatic trades are placed.
          </p>
        </div>
      </div>

      {/* TAB 1: RESEARCH WIZARD */}
      {activeTab === 'wizard' && (
        <div className="wizard-card">
          <h2>Configure Research Criteria</h2>
          <p className="wizard-sub">Define your industry targets and risk preferences to run the hybrid recommendation engine.</p>

          {/* 1. Target Industries */}
          <div className="wizard-section">
            <label className="section-title">
              1. Select Target Industries <span className="req">*</span>
              <span className="section-hint">Choose one or more sectors to evaluate</span>
            </label>
            <div className="industry-grid">
              {INDUSTRIES.map(ind => {
                const selected = selectedIndustries.includes(ind.id);
                return (
                  <button
                    key={ind.id}
                    type="button"
                    className={`industry-pill ${selected ? 'selected' : ''}`}
                    onClick={() => handleToggleIndustry(ind.id)}
                  >
                    <span className="pill-icon">{ind.icon}</span>
                    <span className="pill-label">{ind.label}</span>
                    {selected && <IoCheckmarkCircleOutline className="check-icon" />}
                  </button>
                );
              })}
            </div>
          </div>

          {/* 2. Risk Comfort */}
          <div className="wizard-section">
            <label className="section-title">
              2. Risk Comfort Level
              <span className="section-hint">Aligns volatility & debt parameters</span>
            </label>
            <div className="selection-cards-grid">
              {RISK_LEVELS.map(r => (
                <div
                  key={r.id}
                  className={`selection-card ${riskPreference === r.id ? 'active' : ''}`}
                  onClick={() => setRiskPreference(r.id)}
                >
                  <div className="card-top">
                    <h4>{r.label}</h4>
                    {riskPreference === r.id && <span className="active-dot"></span>}
                  </div>
                  <p>{r.desc}</p>
                </div>
              ))}
            </div>
          </div>

          {/* 3. Horizon & Goal */}
          <div className="wizard-grid-2">
            <div className="wizard-section">
              <label className="section-title">3. Investment Horizon</label>
              <div className="selection-vertical">
                {HORIZONS.map(h => (
                  <div
                    key={h.id}
                    className={`selection-card compact ${investmentHorizon === h.id ? 'active' : ''}`}
                    onClick={() => setInvestmentHorizon(h.id)}
                  >
                    <h4>{h.label}</h4>
                    <p>{h.desc}</p>
                  </div>
                ))}
              </div>
            </div>

            <div className="wizard-section">
              <label className="section-title">4. Primary Investment Goal</label>
              <div className="selection-vertical">
                {GOALS.map(g => (
                  <div
                    key={g.id}
                    className={`selection-card compact ${investmentGoal === g.id ? 'active' : ''}`}
                    onClick={() => setInvestmentGoal(g.id)}
                  >
                    <h4>{g.label}</h4>
                  </div>
                ))}
              </div>

              <div className="budget-box" style={{ marginTop: '1rem' }}>
                <label className="section-title">Target Budget Range</label>
                <input
                  type="text"
                  className="discovery-input"
                  value={budgetRange}
                  onChange={(e) => setBudgetRange(e.target.value)}
                  placeholder="e.g. ₹50,000 - ₹2,00,000"
                />
              </div>
            </div>
          </div>

          {/* 4. Portfolio Context Source */}
          <div className="wizard-section">
            <label className="section-title">
              5. Portfolio Context Integration
              <span className="section-hint">Checks current sector concentration to prevent over-allocation</span>
            </label>
            <div className="portfolio-source-toggle">
              <label className={`source-option ${portfolioSource === 'NEXORA_PORTFOLIO' ? 'active' : ''}`}>
                <input
                  type="radio"
                  name="portfolioSource"
                  checked={portfolioSource === 'NEXORA_PORTFOLIO'}
                  onChange={() => setPortfolioSource('NEXORA_PORTFOLIO')}
                />
                <div>
                  <strong>Connect My Nexora Portfolio</strong>
                  <p>Evaluates existing holdings; penalizes over-concentration and rewards diversification</p>
                </div>
              </label>

              <label className={`source-option ${portfolioSource === 'NONE' ? 'active' : ''}`}>
                <input
                  type="radio"
                  name="portfolioSource"
                  checked={portfolioSource === 'NONE'}
                  onChange={() => setPortfolioSource('NONE')}
                />
                <div>
                  <strong>Continue Without Portfolio</strong>
                  <p>Pure industry screening; portfolio fit will be labeled as "Not assessed"</p>
                </div>
              </label>
            </div>
          </div>

          {/* CTA */}
          <div className="wizard-actions">
            <button
              className="run-analysis-btn"
              onClick={handleRunAnalysis}
              disabled={analyzing}
            >
              {analyzing ? (
                <>
                  <div className="spinner-border"></div>
                  <span>Evaluating ({analysisStep}/4)...</span>
                </>
              ) : (
                <>
                  <IoSparkles />
                  <span>Run AI Stock Discovery</span>
                </>
              )}
            </button>
          </div>

          {analyzing && (
            <div className="loading-progress-panel">
              <div className="progress-bar-container">
                <div className="progress-fill" style={{ width: `${analysisStep * 25}%` }}></div>
              </div>
              <p className="loading-step-text">
                {analysisStep === 1 && 'Checking portfolio holdings and calculating sector exposures...'}
                {analysisStep === 2 && 'Filtering eligible market universe and verifying fundamental ratios...'}
                {analysisStep === 3 && 'Executing transparent 100-point multi-factor weighted scoring...'}
                {analysisStep === 4 && 'Synthesizing grounded plain-language explanations & risk trade-offs...'}
              </p>
            </div>
          )}
        </div>
      )}

      {/* TAB 2: ANALYSIS RESULTS */}
      {activeTab === 'results' && analysisData && (
        <div className="results-container">
          {/* Portfolio Exposure Card */}
          {analysisData.portfolioSummary && analysisData.portfolioSummary.totalHoldings > 0 && (
            <div className="portfolio-exposure-summary">
              <div className="portfolio-exposure-header">
                <h3>📊 Your Current Portfolio Sector Allocation</h3>
                <span className="total-val">Total Holdings: {analysisData.portfolioSummary.totalHoldings}</span>
              </div>

              <div className="sector-bars-grid">
                {analysisData.portfolioSummary.sectorExposure.map(exp => (
                  <div key={exp.sector} className="sector-bar-item">
                    <div className="sector-bar-labels">
                      <span className="sec-name">{exp.sector}</span>
                      <span className="sec-pct">{exp.weightPct}%</span>
                    </div>
                    <div className="sec-track">
                      <div
                        className={`sec-fill ${exp.weightPct > 45 ? 'heavy' : exp.weightPct > 25 ? 'moderate' : 'light'}`}
                        style={{ width: `${Math.min(exp.weightPct, 100)}%` }}
                      ></div>
                    </div>
                  </div>
                ))}
              </div>

              {analysisData.portfolioSummary.warnings?.length > 0 && (
                <div className="concentration-warning-box">
                  <IoWarningOutline className="warning-icon" />
                  <div>
                    {analysisData.portfolioSummary.warnings.map((w, idx) => (
                      <p key={idx}>{w}</p>
                    ))}
                  </div>
                </div>
              )}
            </div>
          )}

          {/* Results Toolbar */}
          <div className="results-toolbar">
            <div className="toolbar-left">
              <h3>Ranked Research Candidates ({filteredRecommendations.length})</h3>
              <span className="data-stamp">Scoring Model: {analysisData.scoringVersion} • Data as of: {analysisData.marketDataAsOf}</span>
            </div>

            <div className="toolbar-right">
              <div className="filter-pill-group">
                <button className={`filter-btn ${filterScore === 'ALL' ? 'active' : ''}`} onClick={() => setFilterScore('ALL')}>
                  All
                </button>
                <button className={`filter-btn ${filterScore === 'STRONG' ? 'active' : ''}`} onClick={() => setFilterScore('STRONG')}>
                  Strong Match (80+)
                </button>
                <button className={`filter-btn ${filterScore === 'REASONABLE' ? 'active' : ''}`} onClick={() => setFilterScore('REASONABLE')}>
                  Reasonable (65-79)
                </button>
              </div>

              {compareSymbols.length >= 2 && (
                <button className="compare-launch-btn" onClick={handleOpenCompare}>
                  <IoGitCompareOutline />
                  <span>Compare Selected ({compareSymbols.length})</span>
                </button>
              )}
            </div>
          </div>

          {/* Candidate Cards Grid */}
          <div className="candidates-list">
            {filteredRecommendations.map(rec => {
              const isWatchlisted = watchlist.some(w => w.symbol === rec.symbol);
              const isCompared = compareSymbols.includes(rec.symbol);

              return (
                <div key={rec.symbol} className="candidate-card">
                  <div className="candidate-card-header">
                    <div className="cand-identity">
                      <span className="rank-tag">#{rec.rank}</span>
                      <div>
                        <div className="symbol-row">
                          <span className="cand-symbol">{rec.symbol}</span>
                          <span className="cand-exchange">{rec.exchange}</span>
                          <span className="cand-industry">{rec.industry}</span>
                        </div>
                        <h4 className="cand-name">{rec.companyName}</h4>
                      </div>
                    </div>

                    <div className="cand-score-box">
                      <div className={`score-badge ${rec.score >= 80 ? 'high' : rec.score >= 65 ? 'med' : 'low'}`}>
                        <span className="score-num">{rec.score}</span>
                        <span className="score-max">/100</span>
                      </div>
                      <span className="score-label">{rec.scoreCategory}</span>
                    </div>
                  </div>

                  {/* Portfolio Fit Alert */}
                  <div className={`portfolio-fit-banner ${rec.scoreBreakdown?.portfolioFit <= 6 ? 'warning' : 'good'}`}>
                    <span>{rec.portfolioFit}</span>
                  </div>

                  {/* Score Breakdown Progress */}
                  <div className="score-breakdown-row">
                    <div className="breakdown-stat">
                      <span className="stat-label">Quality</span>
                      <span className="stat-val">{rec.scoreBreakdown?.quality}/25</span>
                    </div>
                    <div className="breakdown-stat">
                      <span className="stat-label">Valuation</span>
                      <span className="stat-val">{rec.scoreBreakdown?.valuation}/20</span>
                    </div>
                    <div className="breakdown-stat">
                      <span className="stat-label">Growth</span>
                      <span className="stat-val">{rec.scoreBreakdown?.growth}/15</span>
                    </div>
                    <div className="breakdown-stat">
                      <span className="stat-label">Risk</span>
                      <span className="stat-val">{rec.scoreBreakdown?.risk}/15</span>
                    </div>
                    <div className="breakdown-stat">
                      <span className="stat-label">Industry</span>
                      <span className="stat-val">{rec.scoreBreakdown?.industryFit}/10</span>
                    </div>
                    <div className="breakdown-stat">
                      <span className="stat-label">Portfolio Fit</span>
                      <span className="stat-val">{rec.scoreBreakdown?.portfolioFit}/15</span>
                    </div>
                  </div>

                  {/* Key Metrics Grid */}
                  <div className="metrics-strip">
                    <div className="metric-chip">
                      <span className="lbl">Price:</span>
                      <span className="val">₹{rec.currentPrice?.toLocaleString() || 'N/A'}</span>
                    </div>
                    <div className="metric-chip">
                      <span className="lbl">P/E:</span>
                      <span className="val">{rec.keyMetrics?.PE_RATIO ? `${rec.keyMetrics.PE_RATIO}x` : 'N/A'}</span>
                    </div>
                    <div className="metric-chip">
                      <span className="lbl">ROE:</span>
                      <span className="val">{rec.keyMetrics?.ROE ? `${rec.keyMetrics.ROE}%` : 'N/A'}</span>
                    </div>
                    <div className="metric-chip">
                      <span className="lbl">Debt/Eq:</span>
                      <span className="val">{rec.keyMetrics?.DEBT_TO_EQUITY ? `${rec.keyMetrics.DEBT_TO_EQUITY}x` : 'N/A'}</span>
                    </div>
                    <div className="metric-chip">
                      <span className="lbl">Rev YoY:</span>
                      <span className="val">{rec.keyMetrics?.REV_GROWTH_YOY ? `${rec.keyMetrics.REV_GROWTH_YOY}%` : 'N/A'}</span>
                    </div>
                  </div>

                  {/* Bullet Highlights */}
                  <div className="card-points-grid">
                    <div className="points-col">
                      <span className="col-header">Why It Appeared:</span>
                      <ul>
                        {rec.reasons?.slice(0, 3).map((r, i) => (
                          <li key={i}>{r}</li>
                        ))}
                      </ul>
                    </div>
                    <div className="points-col">
                      <span className="col-header">Things to Review:</span>
                      <ul className="risks-list">
                        {rec.risks?.slice(0, 2).map((r, i) => (
                          <li key={i}>{r}</li>
                        ))}
                      </ul>
                    </div>
                  </div>

                  {/* Card Actions */}
                  <div className="card-actions-bar">
                    <button
                      className="action-link-btn"
                      onClick={() => setSelectedStockDetail(rec)}
                    >
                      <IoSparkles />
                      <span>AI Deep Dive</span>
                    </button>

                    <button
                      className={`action-btn-secondary ${isCompared ? 'active' : ''}`}
                      onClick={() => handleToggleCompare(rec.symbol)}
                    >
                      <IoGitCompareOutline />
                      <span>{isCompared ? 'Compared' : 'Compare'}</span>
                    </button>

                    <button
                      className={`action-btn-secondary ${isWatchlisted ? 'watchlisted' : ''}`}
                      onClick={() => isWatchlisted ? handleRemoveFromWatchlist(rec.instrumentId, rec.symbol) : handleAddToWatchlist(rec.instrumentId, rec.symbol)}
                    >
                      {isWatchlisted ? <IoBookmark /> : <IoBookmarkOutline />}
                      <span>{isWatchlisted ? 'Watchlisted' : 'Watchlist'}</span>
                    </button>
                  </div>
                </div>
              );
            })}
          </div>
        </div>
      )}

      {/* TAB 3: WATCHLIST */}
      {activeTab === 'watchlist' && (
        <div className="watchlist-panel">
          <div className="panel-header">
            <h3>Saved Watchlist Items ({watchlist.length})</h3>
            <p className="panel-sub">Tracks candidate companies and data update timestamps. A watchlist is not a purchase order.</p>
          </div>

          {watchlist.length === 0 ? (
            <div className="empty-state">
              <IoBookmarkOutline className="empty-icon" />
              <h4>No stocks in your watchlist yet</h4>
              <p>Run a discovery analysis and click "Watchlist" on candidate cards to track them here.</p>
              <button className="run-analysis-btn" onClick={() => setActiveTab('wizard')}>
                Launch Research Wizard
              </button>
            </div>
          ) : (
            <div className="watchlist-table-wrapper">
              <table className="discovery-table">
                <thead>
                  <tr>
                    <th>Symbol</th>
                    <th>Company Name</th>
                    <th>Sector / Industry</th>
                    <th>Price</th>
                    <th>Market Cap</th>
                    <th>Added On</th>
                    <th>Actions</th>
                  </tr>
                </thead>
                <tbody>
                  {watchlist.map(item => (
                    <tr key={item.id}>
                      <td><strong>{item.symbol}</strong></td>
                      <td>{item.companyName}</td>
                      <td>{item.industry}</td>
                      <td>₹{item.currentPrice?.toLocaleString() || 'N/A'}</td>
                      <td>₹{(item.marketCap / 10000000)?.toFixed(1)} Cr</td>
                      <td>{new Date(item.addedAt).toLocaleDateString()}</td>
                      <td>
                        <button
                          className="table-del-btn"
                          onClick={() => handleRemoveFromWatchlist(item.instrumentId, item.symbol)}
                        >
                          Remove
                        </button>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </div>
      )}

      {/* AI DEEP DIVE MODAL */}
      {selectedStockDetail && (
        <div className="modal-backdrop" onClick={() => setSelectedStockDetail(null)}>
          <div className="deep-dive-modal" onClick={e => e.stopPropagation()}>
            <div className="modal-header">
              <div className="modal-title-left">
                <IoSparkles className="sparkle-icon" />
                <div>
                  <h3>AI Grounded Research: {selectedStockDetail.companyName} ({selectedStockDetail.symbol})</h3>
                  <span className="modal-meta">{selectedStockDetail.industry} • Score: {selectedStockDetail.score}/100 ({selectedStockDetail.scoreCategory})</span>
                </div>
              </div>
              <button className="close-btn" onClick={() => setSelectedStockDetail(null)}>
                <IoCloseOutline />
              </button>
            </div>

            <div className="modal-body">
              {/* Plain Language Summary */}
              <div className="modal-section-box highlight">
                <h4>Plain-Language Summary</h4>
                <p>{selectedStockDetail.aiExplanation?.plainLanguageSummary}</p>
              </div>

              {/* Supporting Evidence */}
              <div className="modal-section-box">
                <h4>Why It Matches Your Criteria</h4>
                <ul className="modal-bullet-list">
                  {selectedStockDetail.aiExplanation?.whyItMatches?.map((w, i) => (
                    <li key={i}>{w}</li>
                  ))}
                </ul>
              </div>

              {/* Risks to Consider */}
              <div className="modal-section-box risk">
                <h4>Material Risks & Trade-Offs</h4>
                <ul className="modal-bullet-list risk">
                  {selectedStockDetail.aiExplanation?.risksToConsider?.map((r, i) => (
                    <li key={i}>{r}</li>
                  ))}
                </ul>
              </div>

              {/* Portfolio Impact */}
              <div className="modal-section-box">
                <h4>Portfolio Concentration Impact</h4>
                <p>{selectedStockDetail.aiExplanation?.portfolioImpact}</p>
              </div>

              {/* Data Limitations */}
              <div className="modal-section-box meta">
                <h4>Verified Data & Timestamps</h4>
                <ul className="limitations-list">
                  {selectedStockDetail.aiExplanation?.dataLimitations?.map((l, i) => (
                    <li key={i}>{l}</li>
                  ))}
                </ul>
              </div>

              {/* Disclaimer */}
              <div className="modal-disclaimer">
                <p>{selectedStockDetail.aiExplanation?.disclaimer}</p>
              </div>
            </div>

            <div className="modal-footer">
              <button className="btn-modal-close" onClick={() => setSelectedStockDetail(null)}>
                Close Research View
              </button>
            </div>
          </div>
        </div>
      )}

      {/* COMPARE MODAL */}
      {showCompareModal && compareData && (
        <div className="modal-backdrop" onClick={() => setShowCompareModal(false)}>
          <div className="compare-modal" onClick={e => e.stopPropagation()}>
            <div className="modal-header">
              <h3>Side-by-Side Candidate Comparison</h3>
              <button className="close-btn" onClick={() => setShowCompareModal(false)}>
                <IoCloseOutline />
              </button>
            </div>

            <div className="modal-body">
              <p className="compare-sub">{compareData.comparisonSummary}</p>

              <div className="compare-table-wrapper">
                <table className="compare-table">
                  <thead>
                    <tr>
                      <th>Metric / Attribute</th>
                      {compareData.candidates?.map(c => (
                        <th key={c.symbol}>
                          <div className="th-cand">
                            <span className="th-sym">{c.symbol}</span>
                            <span className="th-name">{c.companyName}</span>
                          </div>
                        </th>
                      ))}
                    </tr>
                  </thead>
                  <tbody>
                    <tr>
                      <td className="field-name">Industry</td>
                      {compareData.candidates?.map(c => <td key={c.symbol}>{c.industry}</td>)}
                    </tr>
                    <tr>
                      <td className="field-name">Price</td>
                      {compareData.candidates?.map(c => <td key={c.symbol}>₹{c.currentPrice?.toLocaleString()}</td>)}
                    </tr>
                    <tr>
                      <td className="field-name">ROE</td>
                      {compareData.candidates?.map(c => <td key={c.symbol} className="bold-val">{c.keyMetrics?.ROE}%</td>)}
                    </tr>
                    <tr>
                      <td className="field-name">P/E Ratio</td>
                      {compareData.candidates?.map(c => <td key={c.symbol}>{c.keyMetrics?.PE_RATIO}x</td>)}
                    </tr>
                    <tr>
                      <td className="field-name">Debt / Equity</td>
                      {compareData.candidates?.map(c => <td key={c.symbol}>{c.keyMetrics?.DEBT_TO_EQUITY}x</td>)}
                    </tr>
                    <tr>
                      <td className="field-name">Rev Growth YoY</td>
                      {compareData.candidates?.map(c => <td key={c.symbol}>{c.keyMetrics?.REV_GROWTH_YOY}%</td>)}
                    </tr>
                  </tbody>
                </table>
              </div>

              {compareData.keyDifferences?.length > 0 && (
                <div className="differences-box">
                  <h4>Key Differences Highlighted:</h4>
                  <ul>
                    {compareData.keyDifferences.map((d, i) => (
                      <li key={i}>{d}</li>
                    ))}
                  </ul>
                </div>
              )}
            </div>

            <div className="modal-footer">
              <button className="btn-modal-close" onClick={() => setShowCompareModal(false)}>
                Done
              </button>
            </div>
          </div>
        </div>
      )}

      {/* FLOATING AI ASSISTANT TRIGGER */}
      <button
        className="floating-ai-assistant-btn"
        onClick={() => setChatOpen(!chatOpen)}
        title="Open AI Research Assistant"
      >
        <IoChatbubbleEllipsesOutline />
        <span>Ask AI Assistant</span>
      </button>

      {/* AI RESEARCH ASSISTANT DRAWER */}
      {chatOpen && (
        <div className="ai-chat-drawer">
          <div className="drawer-header">
            <div className="drawer-title">
              <IoSparkles className="sparkle-icon" />
              <div>
                <h4>Nexora Research Assistant</h4>
                <span>Grounded in current analysis data</span>
              </div>
            </div>
            <button className="close-drawer-btn" onClick={() => setChatOpen(false)}>
              <IoCloseOutline />
            </button>
          </div>

          <div className="drawer-body">
            <div className="chat-messages-scroll">
              {chatMessages.map((m, i) => (
                <div key={i} className={`chat-bubble-row ${m.sender}`}>
                  <div className={`chat-bubble ${m.sender}`}>
                    <p>{m.text}</p>
                    {m.citations && (
                      <div className="citations-box">
                        <small>Citations: {m.citations.join(', ')}</small>
                      </div>
                    )}
                    <span className="bubble-time">{m.time}</span>
                  </div>
                </div>
              ))}
              {chatLoading && (
                <div className="chat-bubble-row ai">
                  <div className="chat-bubble ai loading">
                    <span>Evaluating verified facts...</span>
                  </div>
                </div>
              )}
            </div>

            {/* Quick Suggestions */}
            <div className="quick-prompts-bar">
              <span className="qp-lbl">Suggested:</span>
              <button className="qp-btn" onClick={() => handleSendChat('Why did the top stock rank higher than others?')}>
                Why did #1 rank higher?
              </button>
              <button className="qp-btn" onClick={() => handleSendChat('What are the main financial risks?')}>
                What are the main risks?
              </button>
              <button className="qp-btn" onClick={() => handleSendChat('How does this affect my sector diversification?')}>
                Portfolio diversification impact?
              </button>
            </div>

            {/* Input Bar */}
            <div className="drawer-input-row">
              <input
                type="text"
                placeholder="Ask about candidates or trade-offs..."
                value={chatInput}
                onChange={e => setChatInput(e.target.value)}
                onKeyDown={e => e.key === 'Enter' && handleSendChat()}
              />
              <button className="send-chat-btn" onClick={() => handleSendChat()} disabled={chatLoading}>
                <IoSendOutline />
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default AIDiscovery;
