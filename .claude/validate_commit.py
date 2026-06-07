import sys
import json
import re

data = json.load(sys.stdin)
cmd = data.get("tool_input", {}).get("command", "")

if "git commit" not in cmd:
    sys.exit(0)

m = re.search(r"git commit.*?-m ['\"](.+?)['\"]", cmd, re.DOTALL)
if not m:
    sys.exit(0)

msg = m.group(1).strip()
if msg.startswith("$("):
    sys.exit(0)

msg = msg.split("\n")[0]
pattern = r"^(feat|fix|refactor|test|style|chore|docs)\((auth|member|category|product|option|order|wish|infra|global)\): [a-z].+[^.]$"
if not re.match(pattern, msg):
    print(json.dumps({
        "decision": "block",
        "reason": (
            f"커밋 메시지 형식 오류: \"{msg}\"\n"
            "형식: <type>(<scope>): <subject>\n"
            "허용 type: feat|fix|refactor|test|style|chore|docs\n"
            "허용 scope: auth|member|category|product|option|order|wish|infra|global\n"
            "subject: 소문자 시작, 마침표 없음"
        )
    }))

sys.exit(0)
