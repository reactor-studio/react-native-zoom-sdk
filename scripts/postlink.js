'use strict';

const xcode = require('xcode');
const fs = require('fs');
const packageJson = require('./package.json');

const projectName = packageJson.name;
const projectPath = `./ios/${projectName}.xcodeproj`;
const xcodeProject = xcode.project(projectPath);
const pbxProject = xcodeProject.parseSync();
console.log('Adding frameworks...');
pbxProject.addFramework('libsqlite3.tbd');
pbxProject.addFramework('libstdc++.6.0.9.tbd');
pbxProject.addFramework('libz.1.2.5.tbd');

fs.writeFileSync(
  projectPath,
  pbxProject.writeSync()
);