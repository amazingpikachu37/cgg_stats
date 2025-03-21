Arguments names prepended by a `-` are optional, and in order to use an optional parameter, it must be prepended by that flag (given below). Mandatory parameters do not use a flag and must be provided before any optional parameters. Also, all underscores are optional, but are presented here to aid in readability. Commands are case insensitive, but parameters may not be. In addition, all 'day' stats have month and year variants, with the exception of `unique_hour_parts_in_day` which can be viewed by changing the function name to the corresponding time duration, and changing any parameters as necessary.

List of flags: 

Flag|Explanation
-|-
`-thread`,`-t`|The focused thread for the stat (defaults to main)
`-username`,`-u`|The focused user for the stat (defaults to submitter)
`-position`,`-p`|The focused position for the stat (defaults to 1)
`-context`,`-c`|The visible range around the focused user or position in the display of the stat (defaults to 2)
`-start_uuid`,'`su`|The start UUID for which to generate the stat (defaults to 00000000-0000-4000-a000-000000000000)
`-end_uuid`,`-eu`|The end UUID for which to generate the stat (defaults to 99999999-9999-4999-a999-999999999999)
`-start_date`,`-sd`|The start date for which to generate the stat (defaults to 2023-2-22)
`-end_date`,`-ed`|The end date for which to generate the stat (defaults to current day + 1)
`-start_k`,`-sk`|The start k for which to generate the stat (defaults to 0)
`-end_k`,`-ek`|The end k for which to generate the stat (defaults to 2147483647)
`-start_count`,`-sc`|The start count for which to generate the stat (defaults to 0)
`-end_count`,`-ec`|The end count for which to generate the stat (defaults to 2147483647)
`-ksize`,`-ks`|Sets how many gets there are per k (defaults to 1)
`-current`|Request only current stats. Takes no arguments and defaults to false.
`-duration`,`-d`|Sets duration (in seconds) for length of "hour". Argument must divide 3600 (defaults to 3600)
`-combined`|Individually combine results from all threads as opposed to treating the count as a single thread. Takes no arguments and defaults to false.
`-percentage`,`-%`|Set the percentage of counts required for a user to have "participated" in a speedrun, unless otherwise specified. Must be between 0 and 1 (defaults to 0.98)
`-counts_between_replies`,`-cbr`|Sets the amount of counts are needed between replies for "perfect" streaks (defaults to 1)
`-mode`|See stat-specific explanation for usage instruction (defaults to 0)
`-min_parts`,`-mp`|See stat-specific explanation for usage instruction (defaults to 0)
`-max_ms`|See stat-specific explanation for usage instruction (defaults to 1000)
`-cap`|See stat-specific explanation for usage instruction (defaults to 0)
`-offset`,`-o`|See stat-specific explanation for usage instruction (defaults to 0)

Note: Format for days is of the form <yyyy-mm-dd>, months <yyyy-mm>, and years <yyyy>.

Additionally, mandatory parameters (if they exist) are always at the front and are not prepended by `-`





The following commands do not have a thread attached to them; they are of the format `!{:} <command>`.

Command|Arguments|Explanation
-|-|-
`help`||Links to this post
`thread_list`|<-t>|Provides a list of all threads. Using the argument displays aliases for that thread.
`update_all`||Downloads new updates for all threads. Admin exclusive.
`exacta` or `trifecta`|<-u> <-c> <-sd> <-ed>|Shows total trifectas/exactas for specified user
`exacta_day_parts` or `trifecta_day_parts`|<-u> <-c> <-sd> <-ed>|Shows total trifecta/exacta day parts for specified user
`exacta_day_streak` or `trifecta_day_streak`|<-mode> <-u> <-c> <-sd> <-ed> <-current>|Shows record trifecta/exacta day streak for specified user. mode=0 -> trifecta streaks, mode=1 -> chalupa exacta streaks, mode=2 -> poonxacta streaks, mode=3 ->  trejexacta streaks, mode<0,mode>=4 -> combined exacta streaks.
`non_unique_exacta_day_streak` or `non_unique_trifecta_day_streak`|<-mode> <-p> <-c> <-sd> <-ed> <-current>|Shows all trifecta/exacta day streaks around a specified position. See above for modes.


The following are commands that can be used after providing a list of threads. To send a list, use space-separated thread names. Example: `!{:} main slow bars <command>`. In addition, there are also some special parameters that can be used here:  
`all_threads` will use all counting threads  
`favorites` or `favourites` will use all side threads in the "favorites" category (before customization)
`traditional` will use all side threads in the "no_mistakes" category (before customization)  
`double` or `double_counting` will use all side threads in the "double_counting" category (before customization)
`no_mistakes_threads` will use all side threads in the "no_mistakes" category (before customization)  
`miscellaneous` or `other` will use all side threads in the "miscellaneous" or "other" categories (before customization)  
In addition, you can prepend any thread with `-` to exclude it.

Example: `!{:} all_sides -tow -tow_avoid0 -tow_double <command>` will use all side threads except for tow threads.


Command|Arguments|Explanation
-|-|-
`hoc`|<-u> <-c> <-su> <-eu> <-cap>|Shows hoc for specified user, where the hoc per thread is capped by <cap>. If cap<=0, there is no cap.
`kparts`|<-u> <-c> <-sk> <-ek> <-ks>|Shows k parts for specified user.
`day_parts`|<-u> <-c> <-sd> <-ed> <-combined>|Shows day parts for specified user
`counts_per_day`|<-u> <-c> <-sd> <-ed>|Shows <username>'s counts per day
`peak_counts_per_day`|<-u> <-c> <-sd> <-ed> <-mp>|Shows <username>'s record counts per day. Users with fewer than <min_parts> day parts are excluded.
`hour_parts`|<-u> <-c> <-su> <-eu> <-d>|Shows hour parts for specified user. 
`hop` or `participation`|<-u> <-c> <-su> <-eu>|Shows hop score for specified user.
`gets`|<-u> <-c> <-sk> <-ek> <-o>|Shows hall of gets. The offset is subtracted from every count (default 0; ex: offset of 1 generates hall of assists)
`assists`|<-u> <-c> <-sk> <-ek> <-o>|Shows hall of assists. This is equivalent to gets, except default offset is 1.
`day`|<start_date> <-u> <-c>|Shows day HoC for specified day
`first_count`|<-u> <-su>|Shows the first count for the specified user.
`last_count`|<-u> <-eu>|Shows the last count for the specified user.
`day_medals` or `cotd`|<-u> <-c> <-sd> <-ed> <-combined>|Shows day medals for particular user. 
`k_medals`|<-u> <-c> <-sk> <-ek> <-ks>|Shows k medals for particular user. 
`day_streak`|<-u> <-c> <-sd> <-ed> <-current> <-mp>|Shows record day streak for specified user. <-mp> is the minimum daily counts required for the streak.
`non_unique_day_streak`|<-p> <-c> <-sd> <-ed> <-mp>|Shows all day streaks around a specified position. <-mp> is the minimum daily counts required for the streak.
`palindromes` or `dromes`|<-u> <-c> <-su> <-eu>|Shows total palindromes for specified user between numbers. Does not work with all threads.
`repdigits`|<-u> <-c> <-su> <-eu>|Shows total repdigits for specified user between numbers. Does not work with all threads.
`n_rep`|<n> <-u> <-c> <-su> <-eu>|Shows total n_reps for specified user between numbers (ex: If n is 3, this will return trips). Does not work with all threads.
`base_n`|<n> <-u> <-c> <-su> <-eu>|Shows total counts in base n for specified user between numbers. Does not work with all threads.
`days_over_n_counts`|<n> <-u> <-c> <-sd> <-ed> <-combined>|Shows total days over n counts for specified user. For example, if <n> equals 5000, this will return total HoEs.
`thread_days_over_n_counts`|<n> <-t> <-c> <-sd> <-ed>|Shows total days over n counts for specified threads.
`day_record`|<-u> <-c> <-sd> <-ed> <-combined>|Shows record counts in a day for specified user.
`top_counts_per_day`|<-p> <-c> <-sd> <-ed> <-combined>|Shows all day hoc amounts around a specified position
`thread_day_record`|<-t> <-c> <-sd> <-ed> <-combined>|Shows record counts in a day for specified thread.
`record_hour`|<-u> <-c> <-su> <-eu> <-d> <-combined>|Shows record hour for specified user.
`record_hour_offset`|<-u> <-c> <-su> <-eu> <-d> <-combined>|Shows record hour for specified user, unaligned to hour boundaries.
`thread_record_hour`|<-p> <-c> <-su> <-eu> <-d>|Shows record hours for individual threads around a specified position.
`dominant_n_counts`|<n> <-u> <-c> <-su> <-eu>|Shows a user's most dominant period of n counts.
`perfect_streak`|<-u> <-c> <-su> <-eu> <-cbr>|Shows longest perfect streak a user has achieved.
`non_unique_perfect_streak`|<-p> <-c> <-su> <-eu> <-cbr>|Shows longest perfect streaks around a specified position.
`rotation_streak` or `double_perfect_streak`|<-p> <-c> <-su> <-eu> <-cbr>|Shows all perfect streaks around a specified position where every person involved maintains a perfect streak.
`fastest_self_replies`|<-p> <-c> <-su> <-eu>|Shows fastest self replies around a specified position. Capped at 1000ms replies.
`median_self_reply`|<-u> <-c> <-su> <-eu> <-mp>|Shows median self reply time for a user. Users with fewer than <-mp> counts are excluded.
`sub_n_ms_self_reply_streak`|<n> <-u> <-c> <-su> <-eu>|Shows how many self replies at or below n ms a specified user has had consecutively.
`counting_pairs`|<username1> <-u> <-c> <-su> <-eu>|Shows the amount of counts <username1> and <-u> have done together.
`hour_counting_pairs`|<username1> <-u> <-c> <-su> <-eu> <-d>|Shows the record amount of counts <username1> and <-u> have done together within an hour.
`user_counting_pairs`|<username1> <-u> <-c> <-su> <-eu>|Shows the counting pairs of <username1> focused on <-u>.
`percent_of_counts_with_user`|<username1> <-u> <-c> <-su> <-eu> <-mp>|Shows the percentage of <username1>'s counts that have been done with <-u>. Users who have done fewer than <-mp> counts will be excluded.
`favorite_counter` or `favourite_counter`|<-u> <-c> <-su> <-eu>|Shows who a specified user has the most counts with.
`time_spent_having_last_count`|<-u> <-c> <-su> <-eu> <-combined>|Shows how long a specified user has had the last count.
`n_ms_replies`|<n> <-u> <-c> <-su> <-eu>|Shows how many n ms replies a specified user has had.
`odd_ratio`|<-u> <-c> <-su> <-eu> <-mp>|Shows the % of counts <username> made that were odd. Excludes anyone with fewer counts than <-mp>
`even_ratio`|<-u> <-c> <-su> <-eu> <-mp>|Shows the % of counts <username> made that were even. Excludes anyone with fewer counts than <-mp> 
`unique_hour_parts_in_day`|<-u> <-c> <-su> <-eu> <-d>|Shows how many unique hours a user has participated in (out of 24). For memory reasons, users with fewer counts than 1/2 of the maximum are excluded.
`unique_hour_parts_in_week`|<-u> <-c> <-su> <-eu> <-d>|Shows how many unique hours a user has participated in (out of 168). For memory reasons, users with fewer counts than 1/2 of the maximum are excluded.
`daily_percent_of_counts`|<-p> <-c> <-sd> <-ed>|Shows what percentage of daily counts users have achieved.
`nth_place_daily_percent_of_counts`|<n> <-p> <-c> <-sd> <-ed>|Shows what percentage of daily counts users have achieved at a certain day hoc position.
`daily_photo_finish`|<-p> <-c> <-sd> <-ed> <-%> <-mp>|Shows days where a position in the top 3 was determined by less than <-%>. Only users with more than <-mp> counts are considered.
`largest_cliques`|<-u> <-c> <-su> <-eu> <-mp>|Shows the largest clique <username> is part of, where all users have at least <-mp> counts with each other.
`highest_user_not_counted_with`|<-u> <-c> <-su> <-eu>|Shows the users with the most counts that <-u> has not counted with.
`perfect_ks` or `p500`|<-u> <-c> <-sk> <-ek> <-ks>|Shows total perfect ks <-u> has performed.
`nth_count`|<n> <-u> <-c> <-su> <-eu>|Shows a user's nth count after <-su>.
`nth_count_no_user`|<n> <-su> <-eu>|Shows the nth count after <-su>.
`update_hoc`|<-u> <-c> <-su> <-eu>|Shows total updates for specified user.
`updates_with_string`|<query> <-u> <-c> <-su> <-eu>|Shows total updates a specified user has sent containing <query>.
`fastest_n_counts`|<n> <-u> <-c> <-su> <-eu> <-combined>|Shows the fastest a user has had an n count increase in the hoc.
`slowest_n_counts`|<n> <-u> <-c> <-su> <-eu> <-combined>|Shows the slowest a user has had an n count increase in the hoc.
`thread_fastest_n_counts`|<n> <-t> <-c> <-su> <-eu>|Shows the fastest a thread has received n counts.
`thread_slowest_n_counts`|<n> <-t> <-c> <-su> <-eu>|Shows the slowest a thread has received n counts.
`site_fastest_n_counts`|<n> <-su> <-eu>|Shows the fastest the site has received n counts.
`site_slowest_n_counts`|<n> <-su> <-eu>|Shows the slowest the site has received n counts.
`no_mistake_streak`|<-u> <-c> <-su> <-eu>|Shows record no mistake streak for specified user.
`thread_parts`|<-u> <-c> <-su> <-eu>|Shows unique threads specified user has participated in.
`fastest_bars`|<-p> <-c> <-su> <-eu>|Shows the fastest bars around a specified position
`slowest_bars`|<-p> <-c> <-su> <-eu>|Shows the slowest bars around a specified position
`time_between_hoc_amounts`|<hoc_start> <hoc_end> <-u> <-c> <-su> <-eu>|Shows the time it took a specified user to go between <hoc_start> and <hoc_end> counts.
`sum_of_counts`|<-u> <-c> <-su> <-eu>|Shows the sum of numbers a user has counted.
`sum_of_reciprocals`|<-u> <-c> <-su> <-eu>|Shows the sum of the reciprocals of the numbers a user has counted.
`unique_counts_mod_n`|<n> <-u> <-c> <-su> <-eu>|Shows how many unique counts a user has mod n. For memory reasons, users with fewer counts than n/2 counts are excluded.
`speed_tag`|<users_tagging> <-p> <-c> <-sk> <-ek> <-%>|Displays all speed tags around a given position, with <users_tagging> being how many people are involved in the tag. <-%> is the percentage of counts each user needs in their section (either k_size/<users_tagging> counts, or if artbn tag, either k_size/2 or k_size/(1+<users_tagging>)).
`fastest_splits`|<-p> <-c> <-sk> <-ek> <-%>|Displays the fastest splits around a specified position. Will not work for every thread.
`fastest_splits_by_user`|<-u> <-c> <-sk> <-ek> <-%>|Displays the fastest split for a specified user. Will not work for every thread.
`fastest_ks`|<-p> <-c> <-sk> <-ek> <-ks> <-%>|Displays the fastest ks around a specified position.
`fastest_ks_by_user`|<-u> <-c> <-sk> <-ek> <-ks> <-%>|Displays the fastest k for a specified user.
`pair_pb`|<username1> <-u> <-c> <-sk> <-ek> <-%>|Displays the fastest k <username1> and <-u> have run together where each user has at least <-%> of the counts.
`split_std_dev`|<-p> <-c> <-sk> <-ek> <-%>|Displays the minimum split standard deviation in a k around a specified position. 
`k_streak`|<-u> <-c> <-sk> <-ek> <-ks> <-current>|Shows record k part streak for specified user. 
`non_unique_k_streak`|<-p> <-c> <-sk> <-ek> <-ks>|Shows all k part streaks around a specified position. 
`non_unique_daily_nth_place_streak`|<n> <-p> <-c> <-sd> <-ed> <-ks>|Shows all nth-place day hoc streaks around a specified position. 
`update_db`||Downloads counts for specified threads. Admin exclusive.



The following are commands that will only work with a single thread

Command|Arguments|Explanation
-|-|-
`about` or ``||Provides various metadata for a thread
`k`|<k> <-u> <-c> <-ks>|Shows <size>k HoC for specific k (ex: with <-ks>=100, this displays 100k hoc
`sum_of_best` or `sob`|<-u> <-sk> <-ek> <-%>|Displays a user's sum of best splits. Will not work for every thread.
`splits`|<k>|Displays the splits for a specific k. Will not work for every thread.
`speed`|<k> <-ks>|Displays the speed of the specified k. <size> denotes how many gets there are per k.
`streaks_without_valid_count`|<-p> <-c> <-su> <-eu>|Displays the number of failed attempts for a count. Only considers counts that could have been valid. Must be used with thread 1/x.
`peak_count`|<-p> <-c> <-su> <-eu>|Displays the greatest count reached in the thread. Must be used with a "no mistakes" thread.
`update`|<update_id>|Gets the specified update for the thread.
`number` or `count`|<number>|Gets all updates with the specified number for the thread.
