/*
 * Copyright (c) 2010-2015 Pivotal Software, Inc. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you
 * may not use this file except in compliance with the License. You
 * may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * permissions and limitations under the License. See accompanying
 * LICENSE file.
 */
/*
 * Changes for SnappyData data platform.
 *
 * Portions Copyright (c) 2016 SnappyData, Inc. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you
 * may not use this file except in compliance with the License. You
 * may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * permissions and limitations under the License. See accompanying
 * LICENSE file.
 */

/**
 * JSONObject.h
 */

#ifndef JSONOBJECT_H_
#define JSONOBJECT_H_

#include "SQLException.h"

namespace io {
namespace snappydata {
namespace client {
namespace types {

  class JSON {
  private:
    std::shared_ptr<thrift::JSONObject> m_val;

  public:
    JSON(const std::shared_ptr<thrift::JSONObject>& json) : m_val(json) {
    }

    JSON(const std::string& str);

    JSON(const JSON& other);

    JSON& operator=(const JSON& other);

    inline const std::shared_ptr<thrift::JSONObject>& getThriftObject()
        const noexcept {
      return m_val;
    }
  };

} /* namespace types */
} /* namespace client */
} /* namespace snappydata */
} /* namespace io */

#endif /* JSONOBJECT_H_ */
