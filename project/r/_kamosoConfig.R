# _kamosoConfig.R
#
# Daniel Duran, SFB 732 (A2), Universität Stuttgart
#
# ATTENTION: adjust these values according to your setup!

# -------------------------------------------------------------------
# set file names and patterns:

kamoso <- list(
  agent.file = 'agents.csv',
  config.file = 'config.prop',
  base.path   = '/path/to/kamoso/',
  lex.file.pat = '[0-9]+_lexicon\\.csv',
  epochs.file = 'epochs.csv',
  prop.pat = '\\.*\\.prop$',
  err = '.err',
  epochs.max = 1000
)

kamoso.setup.pfxs  <- c(
  "par-dist_", "par-reg_", "par-stat_", 
  "par-dirt-beta_", "par-dirt-gamma_",
  "reg-dist_", "reg-reg_", "reg-stat_", 
  "sw-dist_", "sw-reg_", "sw-stat_")

kamoso.setup.titls <- c('Parochial network topology (closeness interaction)',
  'Parochial network topology (regular interaction)',
  'Parochial network topology (status interaction)',
  'Parochial network topology (closeness interaction; high Beta)',
  'Parochial network topology (closeness interaction; high Gamma)',
  'Regular network topology (closeness interaction)',
  'Regular network topology (regular interaction)',
  'Regular network topology (status interaction)',
  'Small-world network topology (closeness interaction)',
  'Small-world network topology (regular interaction)',
  'Small-world network topology (status interaction)' )
