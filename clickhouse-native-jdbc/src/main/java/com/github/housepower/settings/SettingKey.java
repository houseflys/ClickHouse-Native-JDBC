/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.housepower.settings;

import com.github.housepower.misc.StrUtil;
import com.github.housepower.misc.Validate;
import com.github.housepower.serde.SettingType;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public class SettingKey implements Serializable {
    // key always is lower case
    private static final Map<String, SettingKey> DEFINED_SETTING_KEYS = new ConcurrentHashMap<>();

    public static SettingKey min_compress_block_size = SettingKey.builder()
            .withName("min_compress_block_size")
            .withType(SettingType.Int64)
            .withDescription("The actual size of the block to compress, if the uncompressed data less than max_compress_block_size is no less than this value and no less than the volume of data for one mark.")
            .build();

    public static SettingKey max_compress_block_size = SettingKey.builder()
            .withName("max_compress_block_size")
            .withType(SettingType.Int64)
            .withDescription("The maximum size of blocks of uncompressed data before compressing for writing to a table.")
            .build();

    public static SettingKey max_block_size = SettingKey.builder()
            .withName("max_block_size")
            .withType(SettingType.Int64)
            .withDescription("Maximum block size for reading")
            .build();

    public static SettingKey max_insert_block_size = SettingKey.builder()
            .withName("max_insert_block_size")
            .withType(SettingType.Int64)
            .withDescription("The maximum block size for insertion, if we control the creation of blocks for insertion.")
            .build();

    public static SettingKey min_insert_block_size_rows = SettingKey.builder()
            .withName("min_insert_block_size_rows")
            .withType(SettingType.Int64)
            .withDescription("Squash blocks passed to INSERT query to specified size in rows, if blocks are not big enough.")
            .build();

    public static SettingKey min_insert_block_size_bytes = SettingKey.builder()
            .withName("min_insert_block_size_bytes")
            .withType(SettingType.Int64)
            .withDescription("Squash blocks passed to INSERT query to specified size in bytes, if blocks are not big enough.")
            .build();

    public static SettingKey max_read_buffer_size = SettingKey.builder()
            .withName("max_read_buffer_size")
            .withType(SettingType.Int64)
            .withDescription("The maximum size of the buffer to read from the filesystem.")
            .build();

    public static SettingKey max_distributed_connections = SettingKey.builder()
            .withName("max_distributed_connections")
            .withType(SettingType.Int64)
            .withDescription("The maximum number of connections for distributed processing of one query (should be greater than max_threads).")
            .build();

    public static SettingKey max_query_size = SettingKey.builder()
            .withName("max_query_size")
            .withType(SettingType.Int64)
            .withDescription("Which part of the query can be read into RAM for parsing (the remaining data for INSERT, if any, is read later)")
            .build();

    public static SettingKey interactive_delay = SettingKey.builder()
            .withName("interactive_delay")
            .withType(SettingType.Int64)
            .withDescription("The interval in microseconds to check if the request is cancelled, and to send progress info.")
            .build();

    public static SettingKey connect_timeout = SettingKey.builder()
            .withName("connect_timeout")
            .withType(SettingType.Seconds)
            .withDescription("Connection timeout if there are no replicas.")
            .build();

    public static SettingKey connect_timeout_with_failover_ms = SettingKey.builder()
            .withName("connect_timeout_with_failover_ms")
            .withType(SettingType.Milliseconds)
            .withDescription("Connection timeout for selecting first healthy replica.")
            .build();

    public static SettingKey queue_max_wait_ms = SettingKey.builder()
            .withName("queue_max_wait_ms")
            .withType(SettingType.Milliseconds)
            .withDescription("The wait time in the request queue, if the number of concurrent requests exceeds the maximum.")
            .build();

    public static SettingKey poll_interval = SettingKey.builder()
            .withName("poll_interval")
            .withType(SettingType.Int64)
            .withDescription("Block at the query wait loop on the server for the specified number of seconds.")
            .build();

    public static SettingKey distributed_connections_pool_size = SettingKey.builder()
            .withName("distributed_connections_pool_size")
            .withType(SettingType.Int64)
            .withDescription("Maximum number of connections with one remote server in the pool.")
            .build();

    public static SettingKey connections_with_failover_max_tries = SettingKey.builder()
            .withName("connections_with_failover_max_tries")
            .withType(SettingType.Int64)
            .withDescription("The maximum number of attempts to connect to replicas.")
            .build();

    public static SettingKey extremes = SettingKey.builder()
            .withName("extremes")
            .withType(SettingType.Bool)
            .withDescription("Calculate minimums and maximums of the result columns. They can be output in JSON-formats.")
            .build();

    public static SettingKey use_uncompressed_cache = SettingKey.builder()
            .withName("use_uncompressed_cache")
            .withType(SettingType.Bool)
            .withDescription("Whether to use the cache of uncompressed blocks.")
            .build();

    public static SettingKey replace_running_query = SettingKey.builder()
            .withName("replace_running_query")
            .withType(SettingType.Bool)
            .withDescription("Whether the running request should be canceled with the same id as the new one.")
            .build();

    public static SettingKey background_pool_size = SettingKey.builder()
            .withName("background_pool_size")
            .withType(SettingType.Int64)
            .withDescription("Number of threads performing background work for tables (for example, merging in merge tree). Only has meaning at server startup.")
            .build();

    public static SettingKey background_schedule_pool_size = SettingKey.builder()
            .withName("background_schedule_pool_size")
            .withType(SettingType.Int64)
            .withDescription("Number of threads performing background tasks for replicated tables. Only has meaning at server startup.")
            .build();

    public static SettingKey distributed_directory_monitor_sleep_time_ms = SettingKey.builder()
            .withName("distributed_directory_monitor_sleep_time_ms")
            .withType(SettingType.Milliseconds)
            .withDescription("Sleep time for StorageDistributed DirectoryMonitors in case there is no work or exception has been thrown.")
            .build();

    public static SettingKey distributed_directory_monitor_batch_inserts = SettingKey.builder()
            .withName("distributed_directory_monitor_batch_inserts")
            .withType(SettingType.Bool)
            .withDescription("Should StorageDistributed DirectoryMonitors try to batch individual inserts into bigger ones.")
            .build();

    public static SettingKey optimize_move_to_prewhere = SettingKey.builder()
            .withName("optimize_move_to_prewhere")
            .withType(SettingType.Bool)
            .withDescription("Allows disabling WHERE to PREWHERE optimization in SELECT queries from MergeTree.")
            .build();

    public static SettingKey replication_alter_partitions_sync = SettingKey.builder()
            .withName("replication_alter_partitions_sync")
            .withType(SettingType.Int64)
            .withDescription("Wait for actions to manipulate the partitions. 0 - do not wait, 1 - wait for execution only of itself, 2 - wait for everyone.")
            .build();

    public static SettingKey replication_alter_columns_timeout = SettingKey.builder()
            .withName("replication_alter_columns_timeout")
            .withType(SettingType.Int64)
            .withDescription("Wait for actions to change the table structure within the specified number of seconds. 0 - wait unlimited time.")
            .build();

    public static SettingKey totals_auto_threshold = SettingKey.builder()
            .withName("totals_auto_threshold")
            .withType(SettingType.Float32)
            .withDescription("The threshold for totals_mode = 'auto'.")
            .build();

    public static SettingKey compile = SettingKey.builder()
            .withName("compile")
            .withType(SettingType.Bool)
            .withDescription("Whether query compilation is enabled.")
            .build();

    public static SettingKey compile_expressions = SettingKey.builder()
            .withName("compile_expressions")
            .withType(SettingType.Bool)
            .withDescription("Compile some scalar functions and operators to native code.")
            .build();

    public static SettingKey min_count_to_compile = SettingKey.builder()
            .withName("min_count_to_compile")
            .withType(SettingType.Int64)
            .withDescription("The number of structurally identical queries before they are compiled.")
            .build();

    public static SettingKey group_by_two_level_threshold = SettingKey.builder()
            .withName("group_by_two_level_threshold")
            .withType(SettingType.Int64)
            .withDescription("From what number of keys, a two-level aggregation starts. 0 - the threshold is not set.")
            .build();

    public static SettingKey group_by_two_level_threshold_bytes = SettingKey.builder()
            .withName("group_by_two_level_threshold_bytes")
            .withType(SettingType.Int64)
            .withDescription("From what size of the aggregation state in bytes, a two-level aggregation begins to be used. 0 - the threshold is not set. Two-level aggregation is used when at least one of the thresholds is triggered.")
            .build();

    public static SettingKey distributed_aggregation_memory_efficient = SettingKey.builder()
            .withName("distributed_aggregation_memory_efficient")
            .withType(SettingType.Bool)
            .withDescription("Is the memory-saving mode of distributed aggregation enabled.")
            .build();

    public static SettingKey aggregation_memory_efficient_merge_threads = SettingKey.builder()
            .withName("aggregation_memory_efficient_merge_threads")
            .withType(SettingType.Int64)
            .withDescription("Number of threads to use for merge intermediate aggregation results in memory efficient mode. When bigger, then more memory is consumed. 0 means - same as 'max_threads'.")
            .build();

    public static SettingKey max_threads = SettingKey.builder()
            .withName("max_threads")
            .withType(SettingType.Int64)
            .withDescription("The maximum number of threads to execute the request. By default, it is determined automatically.")
            .build();

    public static SettingKey max_parallel_replicas = SettingKey.builder()
            .withName("max_parallel_replicas")
            .withType(SettingType.Int64)
            .withDescription("The maximum number of replicas of each shard used when the query is executed. For consistency (to get different parts of the same partition), this option only works for the specified sampling key. The lag of the replicas is not controlled.")
            .build();

    public static SettingKey skip_unavailable_shards = SettingKey.builder()
            .withName("skip_unavailable_shards")
            .withType(SettingType.Bool)
            .withDescription("Silently skip unavailable shards.")
            .build();

    public static SettingKey distributed_group_by_no_merge = SettingKey.builder()
            .withName("distributed_group_by_no_merge")
            .withType(SettingType.Bool)
            .withDescription("Do not merge aggregation states from different servers for distributed query processing - in case it is for certain that there are different keys on different shards.")
            .build();

    public static SettingKey merge_tree_min_rows_for_concurrent_read = SettingKey.builder()
            .withName("merge_tree_min_rows_for_concurrent_read")
            .withType(SettingType.Int64)
            .withDescription("If at least as many lines are read from one file, the reading can be parallelized.")
            .build();

    public static SettingKey merge_tree_min_rows_for_seek = SettingKey.builder()
            .withName("merge_tree_min_rows_for_seek")
            .withType(SettingType.Int64)
            .withDescription("You can skip reading more than that number of rows at the price of one seek per file.")
            .build();

    public static SettingKey merge_tree_coarse_index_granularity = SettingKey.builder()
            .withName("merge_tree_coarse_index_granularity")
            .withType(SettingType.Int64)
            .withDescription("If the index segment can contain the required keys, divide it into as many parts and recursively check them. ")
            .build();

    public static SettingKey merge_tree_max_rows_to_use_cache = SettingKey.builder()
            .withName("merge_tree_max_rows_to_use_cache")
            .withType(SettingType.Int64)
            .withDescription("The maximum number of rows per request, to use the cache of uncompressed data. If the request is large, the cache is not used. (For large queries not to flush out the cache.)")
            .build();

    public static SettingKey merge_tree_uniform_read_distribution = SettingKey.builder()
            .withName("merge_tree_uniform_read_distribution")
            .withType(SettingType.Bool)
            .withDescription("Distribute read from MergeTree over threads evenly, ensuring stable average execution time of each thread within one read operation.")
            .build();

    public static SettingKey mysql_max_rows_to_insert = SettingKey.builder()
            .withName("mysql_max_rows_to_insert")
            .withType(SettingType.Int64)
            .withDescription("The maximum number of rows in MySQL batch insertion of the MySQL storage engine")
            .build();

    public static SettingKey optimize_min_equality_disjunction_chain_length = SettingKey.builder()
            .withName("optimize_min_equality_disjunction_chain_length")
            .withType(SettingType.Int64)
            .withDescription("The minimum length of the expression `expr = x1 OR ... expr = xN` for optimization ")
            .build();

    public static SettingKey min_bytes_to_use_direct_io = SettingKey.builder()
            .withName("min_bytes_to_use_direct_io")
            .withType(SettingType.Int64)
            .withDescription("The minimum number of bytes for input/output operations is bypassing the page cache. 0 - disabled.")
            .build();

    public static SettingKey force_index_by_date = SettingKey.builder()
            .withName("force_index_by_date")
            .withType(SettingType.Bool)
            .withDescription("Throw an exception if there is a partition key in a table, and it is not used.")
            .build();

    public static SettingKey force_primary_key = SettingKey.builder()
            .withName("force_primary_key")
            .withType(SettingType.Bool)
            .withDescription("Throw an exception if there is primary key in a table, and it is not used.")
            .build();

    public static SettingKey mark_cache_min_lifetime = SettingKey.builder()
            .withName("mark_cache_min_lifetime")
            .withType(SettingType.Int64)
            .withDescription("If the maximum size of mark_cache is exceeded, delete only records older than mark_cache_min_lifetime seconds.")
            .build();

    public static SettingKey max_streams_to_max_threads_ratio = SettingKey.builder()
            .withName("max_streams_to_max_threads_ratio")
            .withType(SettingType.Float32)
            .withDescription("Allows you to use more sources than the number of threads - to more evenly distribute work across threads. It is assumed that this is a temporary solution, since it will be possible in the future to make the number of sources equal to the number of threads, but for each source to dynamically select available work for itself.")
            .build();

    public static SettingKey network_zstd_compression_level = SettingKey.builder()
            .withName("network_zstd_compression_level")
            .withType(SettingType.Int64)
            .withDescription("Allows you to select the level of ZSTD compression.")
            .build();

    public static SettingKey priority = SettingKey.builder()
            .withName("priority")
            .withType(SettingType.Int64)
            .withDescription("Priority of the query. 1 - the highest, higher value - lower priority; 0 - do not use priorities.")
            .build();

    public static SettingKey log_queries = SettingKey.builder()
            .withName("log_queries")
            .withType(SettingType.Bool)
            .withDescription("Log requests and write the log to the system table.")
            .build();

    public static SettingKey log_queries_cut_to_length = SettingKey.builder()
            .withName("log_queries_cut_to_length")
            .withType(SettingType.Int64)
            .withDescription("If query length is greater than specified threshold (in bytes), then cut query when writing to query log. Also limit length of printed query in ordinary text log.")
            .build();

    public static SettingKey max_concurrent_queries_for_user = SettingKey.builder()
            .withName("max_concurrent_queries_for_user")
            .withType(SettingType.Int64)
            .withDescription("The maximum number of concurrent requests per user.")
            .build();

    public static SettingKey insert_deduplicate = SettingKey.builder()
            .withName("insert_deduplicate")
            .withType(SettingType.Bool)
            .withDescription("For INSERT queries in the replicated table, specifies that deduplication of insertings blocks should be preformed")
            .build();

    public static SettingKey insert_quorum = SettingKey.builder()
            .withName("insert_quorum")
            .withType(SettingType.Int64)
            .withDescription("For INSERT queries in the replicated table, wait writing for the specified number of replicas and linearize the addition of the data. 0 - disabled.")
            .build();

    public static SettingKey select_sequential_consistency = SettingKey.builder()
            .withName("select_sequential_consistency")
            .withType(SettingType.Int64)
            .withDescription("For SELECT queries from the replicated table, throw an exception if the replica does not have a chunk written with the quorum; do not read the parts that have not yet been written with the quorum.")
            .build();

    public static SettingKey table_function_remote_max_addresses = SettingKey.builder()
            .withName("table_function_remote_max_addresses")
            .withType(SettingType.Int64)
            .withDescription("The maximum number of different shards and the maximum number of replicas of one shard in the `remote` function.")
            .build();

    public static SettingKey read_backoff_min_latency_ms = SettingKey.builder()
            .withName("read_backoff_min_latency_ms")
            .withType(SettingType.Milliseconds)
            .withDescription("Setting to reduce the number of threads in case of slow reads. Pay attention only to reads that took at least that much time.")
            .build();

    public static SettingKey read_backoff_max_throughput = SettingKey.builder()
            .withName("read_backoff_max_throughput")
            .withType(SettingType.Int64)
            .withDescription("Settings to reduce the number of threads in case of slow reads. Count events when the read bandwidth is less than that many bytes per second.")
            .build();

    public static SettingKey read_backoff_min_interval_between_events_ms = SettingKey.builder()
            .withName("read_backoff_min_interval_between_events_ms")
            .withType(SettingType.Milliseconds)
            .withDescription("Settings to reduce the number of threads in case of slow reads. Do not pay attention to the event, if the previous one has passed less than a certain amount of time.")
            .build();

    public static SettingKey read_backoff_min_events = SettingKey.builder()
            .withName("read_backoff_min_events")
            .withType(SettingType.Int64)
            .withDescription("Settings to reduce the number of threads in case of slow reads. The number of events after which the number of threads will be reduced.")
            .build();

    public static SettingKey memory_tracker_fault_probability = SettingKey.builder()
            .withName("memory_tracker_fault_probability")
            .withType(SettingType.Float32)
            .withDescription("For testing of `exception safety` - throw an exception every time you allocate memory with the specified probability.")
            .build();

    public static SettingKey enable_http_compression = SettingKey.builder()
            .withName("enable_http_compression")
            .withType(SettingType.Bool)
            .withDescription("Compress the result if the client over HTTP said that it understands data compressed by gzip or deflate.")
            .build();

    public static SettingKey http_zlib_compression_level = SettingKey.builder()
            .withName("http_zlib_compression_level")
            .withType(SettingType.Int64)
            .withDescription("Compression level - used if the client on HTTP said that it understands data compressed by gzip or deflate.")
            .build();

    public static SettingKey http_native_compression_disable_checksumming_on_decompress = SettingKey.builder()
            .withName("http_native_compression_disable_checksumming_on_decompress")
            .withType(SettingType.Bool)
            .withDescription("If you uncompress the POST data from the client compressed by the native format, do not check the checksum.")
            .build();

    public static SettingKey count_distinct_implementation = SettingKey.builder()
            .withName("count_distinct_implementation")
            .withType(SettingType.UTF8)
            .withDescription("What aggregate function to use for implementation of count(DISTINCT ...)")
            .build();

    public static SettingKey output_format_write_statistics = SettingKey.builder()
            .withName("output_format_write_statistics")
            .withType(SettingType.Bool)
            .withDescription("Write statistics about read rows, bytes, time elapsed in suitable output formats.")
            .build();

    public static SettingKey add_http_cors_header = SettingKey.builder()
            .withName("add_http_cors_header")
            .withType(SettingType.Bool)
            .withDescription("Write add http CORS header.")
            .build();

    public static SettingKey input_format_skip_unknown_fields = SettingKey.builder()
            .withName("input_format_skip_unknown_fields")
            .withType(SettingType.Bool)
            .withDescription("Skip columns with unknown names from input data (it works for JSONEachRow and TSKV formats).")
            .build();

    public static SettingKey input_format_values_interpret_expressions = SettingKey.builder()
            .withName("input_format_values_interpret_expressions")
            .withType(SettingType.Bool)
            .withDescription("For Values format: if field could not be parsed by streaming parser, run SQL parser and try to interpret it as SQL expression.")
            .build();

    public static SettingKey output_format_json_quote_64bit_integers = SettingKey.builder()
            .withName("output_format_json_quote_64bit_integers")
            .withType(SettingType.Bool)
            .withDescription("Controls quoting of 64-bit integers in JSON output format.")
            .build();

    public static SettingKey output_format_json_quote_denormals = SettingKey.builder()
            .withName("output_format_json_quote_denormals")
            .withType(SettingType.Bool)
            .withDescription("Enables '+nan', '-nan', '+inf', '-inf' outputs in JSON output format.")
            .build();

    public static SettingKey output_format_pretty_max_rows = SettingKey.builder()
            .withName("output_format_pretty_max_rows")
            .withType(SettingType.Int64)
            .withDescription("Rows limit for Pretty formats.")
            .build();

    public static SettingKey use_client_time_zone = SettingKey.builder()
            .withName("use_client_time_zone")
            .withType(SettingType.Bool)
            .withDescription("Use client timezone for interpreting DateTime string values, instead of adopting server timezone.")
            .build();

    public static SettingKey send_progress_in_http_headers = SettingKey.builder()
            .withName("send_progress_in_http_headers")
            .withType(SettingType.Bool)
            .withDescription("Send progress notifications using X-ClickHouse-Progress headers. Some clients do not support high amount of HTTP headers (Python requests in particular), so it is disabled by default.")
            .build();

    public static SettingKey http_headers_progress_interval_ms = SettingKey.builder()
            .withName("http_headers_progress_interval_ms")
            .withType(SettingType.Int64)
            .withDescription("Do not send HTTP headers X-ClickHouse-Progress more frequently than at each specified interval.")
            .build();

    public static SettingKey fsync_metadata = SettingKey.builder()
            .withName("fsync_metadata")
            .withType(SettingType.Bool)
            .withDescription("Do fsync after changing metadata for tables and databases (.sql files). Could be disabled in case of poor latency on server with high load of DDL queries and high load of disk subsystem.")
            .build();

    public static SettingKey input_format_allow_errors_num = SettingKey.builder()
            .withName("input_format_allow_errors_num")
            .withType(SettingType.Int64)
            .withDescription("Maximum absolute amount of errors while reading text formats (like CSV, TSV). In case of error, if both absolute and relative values are non-zero, and at least absolute or relative amount of errors is lower than corresponding value, will skip until next line and continue.")
            .build();

    public static SettingKey input_format_allow_errors_ratio = SettingKey.builder()
            .withName("input_format_allow_errors_ratio")
            .withType(SettingType.Float32)
            .withDescription("Maximum relative amount of errors while reading text formats (like CSV, TSV). In case of error, if both absolute and relative values are non-zero, and at least absolute or relative amount of errors is lower than corresponding value, will skip until next line and continue.")
            .build();

    public static SettingKey join_use_nulls = SettingKey.builder()
            .withName("join_use_nulls")
            .withType(SettingType.Bool)
            .withDescription("Use NULLs for non-joined rows of outer JOINs. If false, use default value of corresponding columns data type.")
            .build();

    public static SettingKey max_replica_delay_for_distributed_queries = SettingKey.builder()
            .withName("max_replica_delay_for_distributed_queries")
            .withType(SettingType.Int64)
            .withDescription("If set, distributed queries of Replicated tables will choose servers with replication delay in seconds less than the specified value (not inclusive). Zero means do not take delay into account.")
            .build();

    public static SettingKey fallback_to_stale_replicas_for_distributed_queries = SettingKey.builder()
            .withName("fallback_to_stale_replicas_for_distributed_queries")
            .withType(SettingType.Bool)
            .withDescription("Suppose max_replica_delay_for_distributed_queries is set and all replicas for the queried table are stale. If this setting is enabled, the query will be performed anyway, otherwise the error will be reported.")
            .build();

    public static SettingKey preferred_max_column_in_block_size_bytes = SettingKey.builder()
            .withName("preferred_max_column_in_block_size_bytes")
            .withType(SettingType.Int64)
            .withDescription("Limit on max column size in block while reading. Helps to decrease cache misses count. Should be close to L2 cache size.")
            .build();

    public static SettingKey insert_distributed_sync = SettingKey.builder()
            .withName("insert_distributed_sync")
            .withType(SettingType.Bool)
            .withDescription("If setting is enabled, insert query into distributed waits until data will be sent to all nodes in cluster.")
            .build();

    public static SettingKey insert_distributed_timeout = SettingKey.builder()
            .withName("insert_distributed_timeout")
            .withType(SettingType.Int64)
            .withDescription("Timeout for insert query into distributed. Setting is used only with insert_distributed_sync enabled. Zero value means no timeout.")
            .build();

    public static SettingKey distributed_ddl_task_timeout = SettingKey.builder()
            .withName("distributed_ddl_task_timeout")
            .withType(SettingType.Int64)
            .withDescription("Timeout for DDL query responses from all hosts in cluster. Negative value means infinite.")
            .build();

    public static SettingKey stream_flush_interval_ms = SettingKey.builder()
            .withName("stream_flush_interval_ms")
            .withType(SettingType.Milliseconds)
            .withDescription("Timeout for flushing data from streaming storages.")
            .build();

    public static SettingKey format_schema = SettingKey.builder()
            .withName("format_schema")
            .withType(SettingType.UTF8)
            .withDescription("Schema identifier (used by schema-based formats)")
            .build();

    public static SettingKey insert_allow_materialized_columns = SettingKey.builder()
            .withName("insert_allow_materialized_columns")
            .withType(SettingType.Bool)
            .withDescription("If setting is enabled, Allow materialized columns in INSERT.")
            .build();

    public static SettingKey http_connection_timeout = SettingKey.builder()
            .withName("http_connection_timeout")
            .withType(SettingType.Seconds)
            .withDescription("HTTP connection timeout.")
            .build();

    public static SettingKey http_send_timeout = SettingKey.builder()
            .withName("http_send_timeout")
            .withType(SettingType.Seconds)
            .withDescription("HTTP send timeout")
            .build();

    public static SettingKey http_receive_timeout = SettingKey.builder()
            .withName("http_receive_timeout")
            .withType(SettingType.Seconds)
            .withDescription("HTTP receive timeout")
            .build();

    public static SettingKey optimize_throw_if_noop = SettingKey.builder()
            .withName("optimize_throw_if_noop")
            .withType(SettingType.Bool)
            .withDescription("If setting is enabled and OPTIMIZE query didn't actually assign a merge then an explanatory exception is thrown")
            .build();

    public static SettingKey use_index_for_in_with_subqueries = SettingKey.builder()
            .withName("use_index_for_in_with_subqueries")
            .withType(SettingType.Bool)
            .withDescription("Try using an index if there is a subquery or a table expression on the right side of the IN operator.")
            .build();

    public static SettingKey empty_result_for_aggregation_by_empty_set = SettingKey.builder()
            .withName("empty_result_for_aggregation_by_empty_set")
            .withType(SettingType.Bool)
            .withDescription("Return empty result when aggregating without keys on empty set.")
            .build();

    public static SettingKey allow_distributed_ddl = SettingKey.builder()
            .withName("allow_distributed_ddl")
            .withType(SettingType.Bool)
            .withDescription("If it is set to true, then a user is allowed to executed distributed DDL queries.")
            .build();

    public static SettingKey odbc_max_field_size = SettingKey.builder()
            .withName("odbc_max_field_size")
            .withType(SettingType.Int64)
            .withDescription("Max size of filed can be read from ODBC dictionary. Long strings are truncated.")
            .build();

    public static SettingKey max_rows_to_read = SettingKey.builder()
            .withName("max_rows_to_read")
            .withType(SettingType.Int64)
            .withDescription("Limit on read rows from the most 'deep' sources. That is, only in the deepest subquery. When reading from a remote server, it is only checked on a remote server.")
            .build();

    public static SettingKey max_bytes_to_read = SettingKey.builder()
            .withName("max_bytes_to_read")
            .withType(SettingType.Int64)
            .withDescription("Limit on read bytes (after decompression) from the most 'deep' sources. That is, only in the deepest subquery. When reading from a remote server, it is only checked on a remote server.")
            .build();

    public static SettingKey max_result_rows = SettingKey.builder()
            .withName("max_result_rows")
            .withType(SettingType.Int64)
            .withDescription("Limit on result size in rows. Also checked for intermediate data sent from remote servers.")
            .build();

    public static SettingKey max_result_bytes = SettingKey.builder()
            .withName("max_result_bytes")
            .withType(SettingType.Int64)
            .withDescription("Limit on result size in bytes (uncompressed). Also checked for intermediate data sent from remote servers.")
            .build();

    public static SettingKey result_overflow_mode = SettingKey.builder()
            .withName("result_overflow_mode")
            .withType(SettingType.UTF8)
            .withDescription("What to do when the limit is exceeded.")
            .build();

    public static SettingKey min_execution_speed = SettingKey.builder()
            .withName("min_execution_speed")
            .withType(SettingType.Int64)
            .withDescription("In rows per second.")
            .build();

    public static SettingKey timeout_before_checking_execution_speed = SettingKey.builder()
            .withName("timeout_before_checking_execution_speed")
            .withType(SettingType.Seconds)
            .withDescription("Check that the speed is not too low after the specified time has elapsed.")
            .build();

    public static SettingKey max_ast_depth = SettingKey.builder()
            .withName("max_ast_depth")
            .withType(SettingType.Int64)
            .withDescription("Maximum depth of query syntax tree. Checked after parsing.")
            .build();

    public static SettingKey max_ast_elements = SettingKey.builder()
            .withName("max_ast_elements")
            .withType(SettingType.Int64)
            .withDescription("Maximum size of query syntax tree in number of nodes. Checked after parsing.")
            .build();

    public static SettingKey max_expanded_ast_elements = SettingKey.builder()
            .withName("max_expanded_ast_elements")
            .withType(SettingType.Int64)
            .withDescription("Maximum size of query syntax tree in number of nodes after expansion of aliases and the asterisk.")
            .build();

    public static SettingKey readonly = SettingKey.builder()
            .withName("readonly")
            .withType(SettingType.Int64)
            .withDescription("0 - everything is allowed. 1 - only read requests. 2 - only read requests, as well as changing settings, except for the 'readonly' setting.")
            .build();

    public static SettingKey max_rows_in_set = SettingKey.builder()
            .withName("max_rows_in_set")
            .withType(SettingType.Int64)
            .withDescription("Maximum size of the set (in number of elements) resulting from the execution of the IN section.")
            .build();

    public static SettingKey max_bytes_in_set = SettingKey.builder()
            .withName("max_bytes_in_set")
            .withType(SettingType.Int64)
            .withDescription("Maximum size of the set (in bytes in memory) resulting from the execution of the IN section.")
            .build();

    public static SettingKey max_rows_in_join = SettingKey.builder()
            .withName("max_rows_in_join")
            .withType(SettingType.Int64)
            .withDescription("Maximum size of the hash table for JOIN (in number of rows).")
            .build();

    public static SettingKey max_bytes_in_join = SettingKey.builder()
            .withName("max_bytes_in_join")
            .withType(SettingType.Int64)
            .withDescription("Maximum size of the hash table for JOIN (in number of bytes in memory).")
            .build();

    public static SettingKey max_rows_to_transfer = SettingKey.builder()
            .withName("max_rows_to_transfer")
            .withType(SettingType.Int64)
            .withDescription("Maximum size (in rows) of the transmitted external table obtained when the GLOBAL IN/JOIN section is executed.")
            .build();

    public static SettingKey max_bytes_to_transfer = SettingKey.builder()
            .withName("max_bytes_to_transfer")
            .withType(SettingType.Int64)
            .withDescription("Maximum size (in uncompressed bytes) of the transmitted external table obtained when the GLOBAL IN/JOIN section is executed.")
            .build();

    public static SettingKey max_rows_in_distinct = SettingKey.builder()
            .withName("max_rows_in_distinct")
            .withType(SettingType.Int64)
            .withDescription("Maximum number of elements during execution of DISTINCT.")
            .build();

    public static SettingKey max_bytes_in_distinct = SettingKey.builder()
            .withName("max_bytes_in_distinct")
            .withType(SettingType.Int64)
            .withDescription("Maximum total size of state (in uncompressed bytes) in memory for the execution of DISTINCT.")
            .build();

    public static SettingKey max_memory_usage = SettingKey.builder()
            .withName("max_memory_usage")
            .withType(SettingType.Int64)
            .withDescription("Maximum memory usage for processing of single query. Zero means unlimited.")
            .build();

    public static SettingKey max_memory_usage_for_user = SettingKey.builder()
            .withName("max_memory_usage_for_user")
            .withType(SettingType.Int64)
            .withDescription("Maximum memory usage for processing all concurrently running queries for the user. Zero means unlimited.")
            .build();

    public static SettingKey max_memory_usage_for_all_queries = SettingKey.builder()
            .withName("max_memory_usage_for_all_queries")
            .withType(SettingType.Int64)
            .withDescription("Maximum memory usage for processing all concurrently running queries on the server. Zero means unlimited.")
            .build();

    public static SettingKey max_network_bandwidth = SettingKey.builder()
            .withName("max_network_bandwidth")
            .withType(SettingType.Int64)
            .withDescription("The maximum speed of data exchange over the network in bytes per second for a query. Zero means unlimited.")
            .build();

    public static SettingKey max_network_bytes = SettingKey.builder()
            .withName("max_network_bytes")
            .withType(SettingType.Int64)
            .withDescription("The maximum number of bytes (compressed) to receive or transmit over the network for execution of the query.")
            .build();

    public static SettingKey max_network_bandwidth_for_user = SettingKey.builder()
            .withName("max_network_bandwidth_for_user")
            .withType(SettingType.Int64)
            .withDescription("The maximum speed of data exchange over the network in bytes per second for all concurrently running user queries. Zero means unlimited.")
            .build();

    public static SettingKey max_network_bandwidth_for_all_users = SettingKey.builder()
            .withName("max_network_bandwidth_for_all_users")
            .withType(SettingType.Int64)
            .withDescription("The maximum speed of data exchange over the network in bytes per second for all concurrently running queries. Zero means unlimited.")
            .build();

    public static SettingKey format_csv_delimiter = SettingKey.builder()
            .withName("format_csv_delimiter")
            .withType(SettingType.Char)
            .withDescription("The character to be considered as a delimiter in CSV data. If setting with a string, a string has to have a length of 1.")
            .build();

    public static SettingKey enable_conditional_computation = SettingKey.builder()
            .withName("enable_conditional_computation")
            .withType(SettingType.Int64)
            .withDescription("Enable conditional computations")
            .build();

    public static SettingKey allow_experimental_bigint_types = SettingKey.builder()
            .withName("allow_experimental_bigint_types")
            .withType(SettingType.Int64)
            .withDescription("Allow Int128, Int256, UInt256 and Decimal256 types")
            .build();

    public static SettingKey allow_experimental_map_type = SettingKey.builder()
            .withName("allow_experimental_map_type")
            .withType(SettingType.Int32)
            .withDescription("Allow Map field to be use")
            .build();

    public static SettingKey port = SettingKey.builder()
            .withName("port")
            .withType(SettingType.Int32)
            .build();

    public static SettingKey user = SettingKey.builder()
            .withName("user")
            .withType(SettingType.UTF8)
            .build();

    public static SettingKey host = SettingKey.builder()
            .withName("host")
            .withType(SettingType.UTF8)
            .build();

    public static SettingKey database = SettingKey.builder()
            .withName("database")
            .withType(SettingType.UTF8)
            .build();

    public static SettingKey password = SettingKey.builder()
            .withName("password")
            .withType(SettingType.UTF8)
            .isSecret()
            .build();

    public static SettingKey query_timeout = SettingKey.builder()
            .withName("query_timeout")
            .withType(SettingType.Seconds)
            .build();

    public static SettingKey tcp_keep_alive = SettingKey.builder()
            .withName("tcp_keep_alive")
            .withType(SettingType.Bool)
            .build();

    public static SettingKey charset = SettingKey.builder()
            .withName("charset")
            .withType(SettingType.UTF8)
            .withDescription("charset for converting between Bytes and String")
            .build();


    public static Builder builder() {
        return new Builder();
    }

    public static Map<String, SettingKey> definedSettingKeys() {
        return new HashMap<>(DEFINED_SETTING_KEYS);
    }

    private final String name;
    private final SettingType<?> type;
    private final String description;
    private final Object defaultValue;
    private final boolean isSecret;

    private SettingKey(String name, SettingType<?> type, String description, Object defaultValue, boolean isSecret) {
        this.name = name;
        this.type = type;
        this.description = description;
        this.defaultValue = defaultValue;
        this.isSecret = isSecret;
    }

    public String name() {
        return name;
    }

    public String description() {
        return description;
    }

    public SettingType<?> type() {
        return type;
    }

    public Object defaultValue() {
        return defaultValue;
    }

    public boolean isSecret() {
        return isSecret;
    }

    public static class Builder {
        private String name;
        private SettingType<?> type;
        private String description;
        private Object defaultValue = null;
        private boolean isSecret = false;

        public Builder withName(String name) {
            this.name = name;
            return this;
        }

        public Builder withType(SettingType<?> type) {
            this.type = type;
            return this;
        }

        public Builder withDescription(String description) {
            this.description = description;
            return this;
        }

        public Builder withDefaultValue(Object defaultValue) {
            this.defaultValue = defaultValue;
            return this;
        }

        public Builder isSecret() {
            this.isSecret = true;
            return this;
        }

        public SettingKey build() {
            Validate.ensure(StrUtil.isNotBlank(name), "name must not blank");
            Validate.ensure(Objects.nonNull(type), "type must not be null");

            if (StrUtil.isBlank(description)) {
                description = name;
            }

            SettingKey settingKey = new SettingKey(name.toLowerCase(Locale.ROOT), type, description, defaultValue, isSecret);
            SettingKey.DEFINED_SETTING_KEYS.put(name, settingKey);

            return settingKey;
        }
    }

}
