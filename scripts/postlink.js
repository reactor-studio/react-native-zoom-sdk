'use strict';

const xcode = require('xcode');
const fs = require('fs');
const packageJson = require('../../../package.json');

const frameworks = [
  'libsqlite3.tbd',
  'libstdc++.6.0.9.tbd',
  'libz.1.2.5.tbd',
  'VideoToolbox.framework',
];

const projectName = packageJson.name;
const projectPath = `./ios/${projectName}.xcodeproj/project.pbxproj`;
const xcodeProject = xcode.project(projectPath);
const pbxProject = xcodeProject.parseSync();
const target = pbxProject.getFirstTarget().uuid;

console.log('Adding frameworks...');
frameworks.map(framework => {
  pbxProject.addFramework(framework, { link: true, target });
  pbxProject.addStaticLibrary(framework, {
    target
  });
});

fs.writeFileSync(
  projectPath,
  pbxProject.writeSync()
);