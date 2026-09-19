#!/usr/bin/env python3
#
# Copyright (c) 2026 Nishant Mishra
#
# This file is part of Tomato - a minimalist pomodoro timer for Android.
#
# Tomato is free software: you can redistribute it and/or modify it under the terms of the GNU
# General Public License as published by the Free Software Foundation, either version 3 of the
# License, or (at your option) any later version.
#
# Tomato is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even
# the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
# Public License for more details.
#
# You should have received a copy of the GNU General Public License along with Tomato.
# If not, see <https://www.gnu.org/licenses/>.
#
"""Generate the Linux packaging metadata from the fastlane metadata.

``fastlane/metadata/android`` is the single source of truth for the store
listing: app name, short description, full description (all translated by
Weblate) and the per-release changelogs. This script renders it into the two
files Linux packaging consumes, and is run by both the Flatpak manifest and
snapcraft.yaml during the build:

  <output-dir>/metainfo/org.nsh07.pomodoro.metainfo.xml   (AppStream)
  <output-dir>/applications/org.nsh07.pomodoro.desktop    (desktop entry)

Usage:
    python3 linux/generate-metadata.py --output-dir /app/share
"""

import argparse
import datetime
import html
import os
import re
import sys
from html.parser import HTMLParser

APP_ID = "org.nsh07.pomodoro"

# fastlane locale used as the untranslated (C locale) source text.
SOURCE_LOCALE = "en-US"

# AppStream only accepts absolute URLs for screenshots.
SCREENSHOT_BASE_URL = (
    "https://raw.githubusercontent.com/nsh07/Tomato/main"
    "/fastlane/metadata/android/{locale}/images/phoneScreenshots/{name}"
)

# Android locale codes that differ from the POSIX ones.
LEGACY_LANGUAGE_CODES = {"iw": "he", "in": "id", "ji": "yi"}

# Locales whose region must be kept because the regional variant differs.
KEEP_REGION = {"pt-BR"}

# "- item", "* item", and the stray "…- item" a few translations use.
BULLET_RE = re.compile(r"^[\s…·•]*[-*–]\s+")

# AppStream only understands <em> and <code>, so <b> and <i> collapse onto <em>.
INLINE_TAG_RE = re.compile(r"<(/?)(b|i|em|strong)\s*>", re.IGNORECASE)
BR_RE = re.compile(r"<br\s*/?>", re.IGNORECASE)
BLOCK_MARKUP_RE = re.compile(r"</?(p|ul|ol|li)\b", re.IGNORECASE)
EM_TAG_RE = re.compile(r"</?em>")

# Locales whose markup had to be repaired, reported once at the end.
_unbalanced_markup = set()
_current_locale = None


# --------------------------------------------------------------------------
# Inline text -> AppStream inline markup
# --------------------------------------------------------------------------

def balance_emphasis(text):
    """Drop unmatched </em> and close dangling <em>.

    A single malformed tag in a translation would otherwise make the whole
    AppStream file unparseable.
    """
    parts = []
    depth = 0
    position = 0
    repaired = False
    for match in EM_TAG_RE.finditer(text):
        parts.append(text[position:match.start()])
        if match.group(0) == "<em>":
            depth += 1
            parts.append("<em>")
        elif depth:
            depth -= 1
            parts.append("</em>")
        else:
            repaired = True
        position = match.end()
    parts.append(text[position:])

    result = "".join(parts)
    if depth:
        result += "</em>" * depth
        repaired = True
    if repaired and _current_locale:
        _unbalanced_markup.add(_current_locale)
    return result


def render_inline(text):
    """Escape ``text`` for XML, keeping <b>/<i>/<em>/<strong> as <em>."""
    text = html.unescape(text)
    text = BR_RE.sub(" ", text)
    text = INLINE_TAG_RE.sub(
        lambda m: "\x00/em\x01" if m.group(1) else "\x00em\x01", text
    )
    text = re.sub(r"<[^>]*>", "", text)  # drop any other stray markup
    text = html.escape(text, quote=False)
    text = text.replace("\x00", "<").replace("\x01", ">")
    return balance_emphasis(re.sub(r"\s+", " ", text).strip())


# --------------------------------------------------------------------------
# Description / changelog text -> AppStream description blocks
# --------------------------------------------------------------------------
#
# A block is either ("p", "<text>") or ("ul", ["<item>", ...]).

class _HtmlDescriptionParser(HTMLParser):
    """Parses the handful of translations that are written as HTML."""

    def __init__(self):
        HTMLParser.__init__(self, convert_charrefs=True)
        self.blocks = []
        self._buf = []
        self._items = None
        self._depth = 0

    def _take(self):
        text = re.sub(r"\s+", " ", "".join(self._buf)).strip()
        self._buf = []
        return balance_emphasis(text)

    def _flush_paragraph(self):
        text = self._take()
        if text:
            self.blocks.append(("p", text))

    def _flush_item(self):
        text = self._take()
        if text and self._items is not None:
            self._items.append(text)

    def handle_starttag(self, tag, attrs):
        tag = tag.lower()
        if tag == "p":
            self._flush_paragraph()
        elif tag in ("ul", "ol"):
            if self._depth == 0:
                self._flush_paragraph()
                self._items = []
            else:
                # AppStream has no nested lists, so flatten into the same list.
                self._flush_item()
            self._depth += 1
        elif tag == "li":
            self._flush_item()
        elif tag in ("b", "i", "em", "strong"):
            self._buf.append("<em>")
        elif tag == "br":
            self._buf.append(" ")

    def handle_startendtag(self, tag, attrs):
        if tag.lower() == "br":
            self._buf.append(" ")

    def handle_endtag(self, tag):
        tag = tag.lower()
        if tag == "p":
            self._flush_paragraph()
        elif tag in ("ul", "ol"):
            self._flush_item()
            self._depth -= 1
            if self._depth == 0:
                if self._items:
                    self.blocks.append(("ul", self._items))
                self._items = None
        elif tag == "li":
            self._flush_item()
        elif tag in ("b", "i", "em", "strong"):
            self._buf.append("</em>")

    def handle_data(self, data):
        self._buf.append(html.escape(data, quote=False))

    def close(self):
        HTMLParser.close(self)
        if self._items is not None:
            self._flush_item()
            if self._items:
                self.blocks.append(("ul", self._items))
            self._items = None
        self._flush_paragraph()
        return self.blocks


def _parse_plain(text):
    blocks = []
    paragraph = []
    items = None

    def flush_paragraph():
        if paragraph:
            blocks.append(("p", " ".join(paragraph)))
            del paragraph[:]

    def flush_items():
        if items:
            blocks.append(("ul", list(items)))
            del items[:]

    for raw_line in text.splitlines():
        line = raw_line.strip()
        if not line:
            flush_paragraph()
            flush_items()
            continue

        match = BULLET_RE.match(line)
        if match:
            flush_paragraph()
            if items is None:
                items = []
            rendered = render_inline(line[match.end():])
            if rendered:
                items.append(rendered)
        else:
            flush_items()
            rendered = render_inline(line)
            if rendered:
                paragraph.append(rendered)

    flush_paragraph()
    flush_items()
    return blocks


def set_source(name):
    """Name the file currently being converted, for markup warnings."""
    global _current_locale
    _current_locale = name


def parse_description(text):
    """Convert a fastlane description or changelog into AppStream blocks."""
    if BLOCK_MARKUP_RE.search(text):
        parser = _HtmlDescriptionParser()
        parser.feed(text)
        return parser.close()
    return _parse_plain(text)


def render_blocks(blocks, indent, lang=None):
    """Render description blocks as AppStream XML lines.

    Each translation gets its own ``xml:lang`` blocks, so a locale need not
    match the English structure paragraph for paragraph.
    """
    attr = ' xml:lang="%s"' % lang if lang else ""
    pad = " " * indent
    lines = []
    for kind, content in blocks:
        if kind == "p":
            lines.append("%s<p%s>%s</p>" % (pad, attr, content))
        else:
            lines.append("%s<ul>" % pad)
            for item in content:
                lines.append("%s    <li%s>%s</li>" % (pad, attr, item))
            lines.append("%s</ul>" % pad)
    return lines


# --------------------------------------------------------------------------
# fastlane metadata
# --------------------------------------------------------------------------

def read_text(path):
    if not os.path.isfile(path):
        return None
    with open(path, "r", encoding="utf-8") as handle:
        text = handle.read().strip()
    return text or None


def list_locales(metadata_dir):
    locales = [
        name
        for name in os.listdir(metadata_dir)
        if os.path.isdir(os.path.join(metadata_dir, name))
    ]
    return sorted(locales)


def appstream_locale(locale, all_locales):
    """Map a fastlane locale directory name onto a POSIX locale.

    AppStream and desktop entries both expect ``lang_COUNTRY`` with a script
    carried as an ``@modifier``, not the BCP 47 form fastlane uses.
    """
    parts = locale.split("-", 1)
    language = LEGACY_LANGUAGE_CODES.get(parts[0], parts[0])
    if len(parts) == 1:
        return language

    if locale not in KEEP_REGION:
        siblings = [
            other
            for other in all_locales
            if LEGACY_LANGUAGE_CODES.get(other.split("-", 1)[0], other.split("-", 1)[0])
            == language
        ]
        # Drop the region when unambiguous, so a de_AT user still gets de-DE.
        if len(siblings) == 1:
            return language

    region = parts[1]
    if re.match(r"^[A-Za-z]{4}$", region):  # a script, e.g. nan-Hant
        return "%s@%s" % (language, region.capitalize())
    return "%s_%s" % (language, region.upper())


def load_locale_metadata(metadata_dir):
    """Return a list of (fastlane locale, appstream locale, fields) tuples."""
    all_locales = list_locales(metadata_dir)
    entries = []
    for locale in all_locales:
        locale_dir = os.path.join(metadata_dir, locale)
        fields = {
            "name": read_text(os.path.join(locale_dir, "title.txt")),
            "summary": read_text(os.path.join(locale_dir, "short_description.txt")),
            "description": read_text(os.path.join(locale_dir, "full_description.txt")),
        }
        if not any(fields.values()):
            continue
        entries.append((locale, appstream_locale(locale, all_locales), fields))
    return entries


# --------------------------------------------------------------------------
# Releases
# --------------------------------------------------------------------------

def read_gradle_versions(repo_root):
    path = os.path.join(repo_root, "gradle", "libs.versions.toml")
    versions = {}
    with open(path, "r", encoding="utf-8") as handle:
        for line in handle:
            match = re.match(r'\s*(app-versionName|app-versionCode)\s*=\s*"([^"]*)"', line)
            if match:
                versions[match.group(1)] = match.group(2)
    missing = {"app-versionName", "app-versionCode"} - set(versions)
    if missing:
        raise SystemExit("could not read %s from %s" % (", ".join(sorted(missing)), path))
    return versions["app-versionName"], versions["app-versionCode"]


def read_release_index(path):
    index = {}
    with open(path, "r", encoding="utf-8") as handle:
        for line_number, line in enumerate(handle, 1):
            line = line.strip()
            if not line or line.startswith("#"):
                continue
            fields = line.split("\t")
            if len(fields) != 3:
                raise SystemExit("%s:%d: expected 3 tab-separated fields" % (path, line_number))
            code, name, date = (field.strip() for field in fields)
            index[code] = (name, date)
    return index


def build_date():
    """Today, or SOURCE_DATE_EPOCH when the build asks for reproducibility."""
    epoch = os.environ.get("SOURCE_DATE_EPOCH")
    if epoch:
        return datetime.datetime.utcfromtimestamp(int(epoch)).strftime("%Y-%m-%d")
    return datetime.date.today().isoformat()


def collect_releases(changelog_dir, release_index, current_name, current_code):
    """Newest first, as AppStream requires."""
    releases = []
    if not os.path.isdir(changelog_dir):
        return releases

    for filename in os.listdir(changelog_dir):
        if not filename.endswith(".txt"):
            continue
        code = filename[: -len(".txt")]
        if not code.isdigit():
            continue

        if code in release_index:
            version, date = release_index[code]
        elif code == current_code:
            version, date = current_name, build_date()
        else:
            sys.stderr.write(
                "warning: no linux/releases.tsv entry for version code %s, "
                "skipping its changelog\n" % code
            )
            continue

        set_source("%s/changelogs/%s" % (SOURCE_LOCALE, filename))
        text = read_text(os.path.join(changelog_dir, filename))
        releases.append(
            {
                "code": int(code),
                "version": version,
                "date": date,
                "blocks": parse_description(text) if text else [],
            }
        )

    releases.sort(key=lambda release: release["code"], reverse=True)
    return releases


def render_releases(releases, indent=4):
    pad = " " * indent
    if not releases:
        return "%s<releases />" % pad

    lines = ["%s<releases>" % pad]
    for release in releases:
        prerelease = re.search(r"-(alpha|beta|rc)", release["version"], re.IGNORECASE)
        attrs = 'version="%s" date="%s"' % (
            html.escape(release["version"], quote=True),
            release["date"],
        )
        if prerelease:
            attrs += ' type="development"'

        if not release["blocks"]:
            lines.append("%s    <release %s />" % (pad, attrs))
            continue

        lines.append("%s    <release %s>" % (pad, attrs))
        lines.append("%s        <description>" % pad)
        lines.extend(render_blocks(release["blocks"], indent + 12))
        lines.append("%s        </description>" % pad)
        lines.append("%s    </release>" % pad)
    lines.append("%s</releases>" % pad)
    return "\n".join(lines)


# --------------------------------------------------------------------------
# Screenshots
# --------------------------------------------------------------------------

def render_screenshots(metadata_dir, indent=4):
    pad = " " * indent
    screenshot_dir = os.path.join(
        metadata_dir, SOURCE_LOCALE, "images", "phoneScreenshots"
    )
    if not os.path.isdir(screenshot_dir):
        return ""

    def sort_key(name):
        stem = os.path.splitext(name)[0]
        return (0, int(stem)) if stem.isdigit() else (1, 0, name)

    names = sorted(
        (
            name
            for name in os.listdir(screenshot_dir)
            if name.lower().endswith((".png", ".jpg", ".jpeg", ".webp"))
        ),
        key=sort_key,
    )
    if not names:
        return ""

    lines = ["%s<screenshots>" % pad]
    for position, name in enumerate(names):
        kind = ' type="default"' if position == 0 else ""
        url = SCREENSHOT_BASE_URL.format(locale=SOURCE_LOCALE, name=name)
        lines.append("%s    <screenshot%s>" % (pad, kind))
        lines.append("%s        <image>%s</image>" % (pad, html.escape(url, quote=False)))
        lines.append("%s    </screenshot>" % pad)
    lines.append("%s</screenshots>" % pad)
    return "\n".join(lines)


# --------------------------------------------------------------------------
# Rendering
# --------------------------------------------------------------------------

def fill_template(path, replacements):
    with open(path, "r", encoding="utf-8") as handle:
        template = handle.read()

    for key, value in replacements.items():
        placeholder = "@%s@" % key
        if placeholder not in template:
            raise SystemExit("%s: missing placeholder %s" % (path, placeholder))
        template = template.replace(placeholder, value)
    return template


def render_metainfo(template_path, entries, screenshots, releases):
    source = dict(
        (locale, fields)
        for locale, _, fields in entries
        if locale == SOURCE_LOCALE
    ).get(SOURCE_LOCALE)
    if source is None:
        raise SystemExit("fastlane metadata for %s is missing" % SOURCE_LOCALE)
    for field in ("name", "summary", "description"):
        if not source[field]:
            raise SystemExit("fastlane %s/%s is missing" % (SOURCE_LOCALE, field))

    set_source(SOURCE_LOCALE)
    names = ["    <name>%s</name>" % render_inline(source["name"])]
    summaries = ["    <summary>%s</summary>" % render_inline(source["summary"])]
    descriptions = ["    <description>"]
    descriptions.extend(render_blocks(parse_description(source["description"]), 8))

    for locale, lang, fields in entries:
        if locale == SOURCE_LOCALE:
            continue
        set_source(locale)
        if fields["name"]:
            names.append(
                '    <name xml:lang="%s">%s</name>' % (lang, render_inline(fields["name"]))
            )
        if fields["summary"]:
            summaries.append(
                '    <summary xml:lang="%s">%s</summary>'
                % (lang, render_inline(fields["summary"]))
            )
        if fields["description"]:
            descriptions.extend(
                render_blocks(parse_description(fields["description"]), 8, lang)
            )

    descriptions.append("    </description>")

    return fill_template(
        template_path,
        {
            "NAMES": "\n".join(names),
            "SUMMARIES": "\n".join(summaries),
            "DESCRIPTIONS": "\n".join(descriptions),
            "SCREENSHOTS": screenshots,
            "RELEASES": releases,
        },
    )


def desktop_value(text):
    """Escape a value for a desktop entry key."""
    text = render_inline(text)
    text = re.sub(r"</?em>", "", text)
    return text.replace("\\", "\\\\").replace("\n", "\\n")


def render_desktop(template_path, entries):
    source = dict(
        (locale, fields) for locale, _, fields in entries if locale == SOURCE_LOCALE
    )[SOURCE_LOCALE]

    names = ["Name=%s" % desktop_value(source["name"])]
    comments = ["Comment=%s" % desktop_value(source["summary"])]

    for locale, lang, fields in entries:
        if locale == SOURCE_LOCALE:
            continue
        set_source(locale)
        if fields["name"]:
            names.append("Name[%s]=%s" % (lang, desktop_value(fields["name"])))
        if fields["summary"]:
            comments.append("Comment[%s]=%s" % (lang, desktop_value(fields["summary"])))

    return fill_template(
        template_path, {"NAMES": "\n".join(names), "COMMENTS": "\n".join(comments)}
    )


def write(path, content):
    directory = os.path.dirname(path)
    if directory and not os.path.isdir(directory):
        os.makedirs(directory)
    with open(path, "w", encoding="utf-8") as handle:
        handle.write(content)
    print("wrote %s" % path)


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument(
        "--output-dir",
        required=True,
        help="data directory to populate, e.g. /app/share or $CRAFT_PART_INSTALL/usr/share",
    )
    args = parser.parse_args()

    here = os.path.dirname(os.path.abspath(__file__))
    repo_root = os.path.dirname(here)
    metadata_dir = os.path.join(repo_root, "fastlane", "metadata", "android")
    if not os.path.isdir(metadata_dir):
        raise SystemExit("fastlane metadata not found at %s" % metadata_dir)

    entries = load_locale_metadata(metadata_dir)
    current_name, current_code = read_gradle_versions(repo_root)
    releases = collect_releases(
        os.path.join(metadata_dir, SOURCE_LOCALE, "changelogs"),
        read_release_index(os.path.join(here, "releases.tsv")),
        current_name,
        current_code,
    )

    metainfo = render_metainfo(
        os.path.join(here, "metainfo.xml.in"),
        entries,
        render_screenshots(metadata_dir),
        render_releases(releases),
    )
    desktop = render_desktop(os.path.join(here, "tomato.desktop.in"), entries)

    write(os.path.join(args.output_dir, "metainfo", "%s.metainfo.xml" % APP_ID), metainfo)
    write(
        os.path.join(args.output_dir, "applications", "%s.desktop" % APP_ID), desktop
    )
    print(
        "%d locales, %d releases, from fastlane/metadata/android"
        % (len(entries), len(releases))
    )
    if _unbalanced_markup:
        sys.stderr.write(
            "warning: repaired unbalanced <b>/<i> markup in fastlane metadata "
            "for: %s\n" % ", ".join(sorted(_unbalanced_markup))
        )


if __name__ == "__main__":
    main()
