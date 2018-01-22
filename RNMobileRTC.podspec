
require 'json'
package_json = JSON.parse(File.read('package.json'))

Pod::Spec.new do |s|
  s.name         = package_json["name"]
  s.version      = package_json["version"]
  s.summary      = package_json["description"]
  s.homepage     = ""
  s.license      = package_json["license"]
  s.author       = { "Ivan Vukovic" => "3.14wee@gmail.com" }
  s.platform     = :ios, "7.0"
  s.source       = { :git => "https://github.com/IvySpaceDev/react-native-zoom-sdk.git", :tag => "v#{s.version}" }
  s.source_files  = "ios/RNMobileRTC/**/*.{h,m}"
  s.requires_arc = true

  s.dependency "React"
end

  