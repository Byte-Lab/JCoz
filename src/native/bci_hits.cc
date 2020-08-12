#include "bci_hits.h"

#include <sstream>
#include "spdlog/spdlog.h"

void bci_hits::add_hit(char* class_fqn, jmethodID method_id, jint line_number, jint bci)
{
  _freqs[method_id][line_number][bci]++;
  _declaring_classes[method_id] = class_fqn;
}

std::vector<std::string> bci_hits::create_dump(jvmtiEnv* dealloc_jvmti)
{
  std::vector<std::string> result;
  result.emplace_back("Bytecode index hits:");
  for (auto method_it = _freqs.begin(); method_it != _freqs.end(); ++method_it)
  {
    char* class_fqn = _declaring_classes[method_it->first];
    result.push_back(fmt::format("\tFor class {}:", class_fqn));
    for (auto line_it = method_it->second.begin(); line_it != method_it->second.end(); ++line_it)
    {
      jint line_number = line_it->first;
      std::stringstream ss;
      ss << "\t\t" << line_number << ": ";
      for (auto bci_it = line_it->second.begin(); bci_it != line_it->second.end(); ++bci_it)
      {
        ss << fmt::format("({}, {}); ", bci_it->first, bci_it->second);
      }
      result.emplace_back(ss.str());
    }
    dealloc_jvmti->Deallocate(reinterpret_cast<unsigned char*>(class_fqn));
  }
  return result;
}
