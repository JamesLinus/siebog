#!/bin/bash
find ./siebog/war ./siebog-agents/war/radigost -name "*.coffee" | xargs coffee -cb
