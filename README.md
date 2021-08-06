# NestedPlaceholders-PlaceholderAPI-Expansion
Parse nested PAPI placeholders within PAPI placeholders

# Usage
`%nested_<placeholder>%` - Parses the placeholder 2 times  
`%nested_#_<placeholder>%` - Parses the placeholder # times

# Example
`%javascript_test%` returns `%player_name%` which returns a player's name  
`%nested_javascript_test%` or `%nested_2_javascript_test%` will be parsed 2 times, it will first be parsed into %player_name% and then parsed again into the player's name.
