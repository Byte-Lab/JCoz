#ifndef JCOZ_ARGS_H
#define JCOZ_ARGS_H

#include <string>
#include <iostream>

enum profiler_option
{
  _unknown,
  _search_scopes,
  _ignored_scopes,
  _progress_point,
  _end_to_end,
  _warmup,
  _fix_exp
};

namespace agent_args
{
  profiler_option from_string(std::string &option)
  {
    if (option == "pkg" || option == "package" || option == "search") return _search_scopes;
    if (option == "ignore") return _ignored_scopes;
    if (option == "progress-point") return _progress_point;
    if (option == "end-to-end") return _end_to_end;
    if (option == "warmup") return _warmup;
    if (option == "fix-exp") return _fix_exp;

    return _unknown;
  }

  void print_usage()
  {
    std::cout
      << "usage: java -agentpath:<absolute_path_to_agent>="
      << "pkg=<package_name>_"
      << "progress-point=<class:line_no>_"
      << "end-to-end (optional)_"
      << "warmup=<warmup_time_ms> (optional - default 5000 ms)"
      << std::endl;
  }

  void report_error(const char *message)
  {
    std::cerr << message << std::endl;
    print_usage();
    exit(1);
  }
} // namespace agent_args

#endif //JCOZ_ARGS_H
