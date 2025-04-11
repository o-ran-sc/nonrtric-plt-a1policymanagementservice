#
# ============LICENSE_START=======================================================
#  Copyright (C) 2025 OpenInfra Foundation Europe. All rights reserved.
# ================================================================================
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#
# SPDX-License-Identifier: Apache-2.0
# ============LICENSE_END=========================================================
#

import sys
import yaml
import re
from pathlib import Path

if len(sys.argv) != 4:
    print("Usage: inject-info.py <input.yaml> <output.yaml> <oas-info.yaml>")
    sys.exit(1)

input_file = Path(sys.argv[1])
output_file = Path(sys.argv[2])
info_file = Path(sys.argv[3])

# Load info section normally
with open(info_file, "r") as f:
    info_yaml = yaml.safe_load(f)
    info_str = yaml.dump({"info": info_yaml})

# Read original YAML as text
with open(input_file, "r") as f:
    original_text = f.read()

# Replace the existing `info:` block using regex (simple, not perfect YAML parser)
new_text = re.sub(
    r"(?ms)^info:\s+.*?(?=^\S|\Z)",  # same pattern
    lambda match: info_str,         # safe way to inject text with backslashes
    original_text,
)


# Write output
output_file.parent.mkdir(parents=True, exist_ok=True)
with open(output_file, "w") as f:
    f.write(new_text)
