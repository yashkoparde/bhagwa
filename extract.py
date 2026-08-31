import re, base64

html = open('view_and_download_cards.html', 'r', encoding='utf-8').read()
matches = re.findall(r'<img src="data:image/png;base64,([^"]+)"', html)
if len(matches) >= 2:
    open('independence_day_flex_champion.png', 'wb').write(base64.b64decode(matches[0]))
    open('independence_day_flex_transparent.png', 'wb').write(base64.b64decode(matches[1]))
    print("Images extracted successfully!")
else:
    print(f"Found {len(matches)} images instead of 2.")
