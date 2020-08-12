#ifndef JCOZ_BCI_HITS_H
#define JCOZ_BCI_HITS_H

#include "globals.h"
#include <vector>
#include <map>

namespace bci_hits
{
    using hit_freq_t = std::map<jint, unsigned int>;

    void add_hit(char* class_fqn, jmethodID method_id, jint line_number, jint bci);

    std::vector<std::string> create_dump(jvmtiEnv* dealloc_jvmti);

    namespace
    {
      std::map<jmethodID, std::map<jint, hit_freq_t>> _freqs;
      std::map<jmethodID, char*> _declaring_classes;
    }
} // namespace bci_hits

#endif //JCOZ_BCI_HITS_H
